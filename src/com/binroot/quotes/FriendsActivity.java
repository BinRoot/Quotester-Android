package com.binroot.quotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import API.AppEngineAPI;
import API.StorageAPI;
import Model.Friend;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsActivity extends Activity {
	FriendsAdapter fa;
	FriendsTextWatcher ftw;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);

		ListView lv = (ListView) findViewById(R.id.list_friends);
		fa = new FriendsAdapter(StorageAPI.getInstance().getFriends(this));
		lv.setAdapter(fa);
		lv.setOnItemClickListener(fa);
		lv.setOnItemLongClickListener(fa);
		lv.setFastScrollEnabled(true);

		((Button)findViewById(R.id.button_friends_add))
			.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleSearch();
			}
		});

		((Button)findViewById(R.id.button_friends_me))
			.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goToTarget(StorageAPI.getInstance().getUsername(FriendsActivity.this),
						StorageAPI.getInstance().getPassword(FriendsActivity.this));
			}
		});
		
	}

	@Override
	public void onBackPressed() {
		if(fa.searchMode) {
			toggleSearch();
		}
		else {
			super.onBackPressed();
		}
	}

	public void toggleSearch(){
		fa.toggleMode();
		toggleVisibility(((RelativeLayout)findViewById(R.id.relative_friends_topleft)));
		toggleVisibility(((RelativeLayout)findViewById(R.id.relative_friends_topmid)));
		toggleVisibility((findViewById(R.id.edit_friends_search)));
		
		TextView tvTop = ((TextView)findViewById(R.id.text_friend_top));
		if(tvTop.getText().toString().equals("Search:")) {
			final Animation a = AnimationUtils.loadAnimation(FriendsActivity.this, R.anim.translate_search_over);
			a.reset();
			(findViewById(R.id.edit_friends_search)).setAnimation(a);
			
			tvTop.setText("Quotester");
			tvTop.setTextColor(0xff00fff0);
			((Button)findViewById(R.id.button_friends_add)).setBackgroundResource(android.R.drawable.ic_menu_search);
		}
		else {
			final Animation a = AnimationUtils.loadAnimation(FriendsActivity.this, R.anim.translate_search);
			a.reset();
			(findViewById(R.id.edit_friends_search)).setAnimation(a);
			
			tvTop.setText("Search:");
			tvTop.setTextColor(0xffffffff);
			((Button)findViewById(R.id.button_friends_add)).setBackgroundResource(R.drawable.closebutton);
		}

		if(fa.searchMode) {

			EditText et = (EditText)(findViewById(R.id.edit_friends_search));

			ftw = new FriendsTextWatcher(new ArrayList<String>(), fa);
			new GetTargetListTask(ftw, fa).execute();
			et.addTextChangedListener(ftw);
		}

		//((Button)findViewById(R.id.button_friends_add)).setBack
	}

	@Override
	public void onResume() {
		super.onResume();
		if(ftw!=null && fa!=null) {
			fa.setFriends(StorageAPI.getInstance().getFriends(this));
			new GetTargetListTask(ftw, fa).execute();
		}

	}

	private void toggleVisibility(View v) {
		if(v.getVisibility()==View.VISIBLE) {
			v.setVisibility(View.GONE);
		}
		else {
			v.setVisibility(View.VISIBLE);
		}
	}

	public class FriendsTextWatcher implements TextWatcher {

		ArrayList<String> targetList;
		FriendsAdapter fa;

		public FriendsTextWatcher(ArrayList<String> targetList, FriendsAdapter fa) {
			this.targetList = targetList;
			this.fa = fa;
		}

		public void setTargetList(ArrayList<String> targetList) {
			this.targetList = targetList;
		}

		@Override
		public void afterTextChanged(Editable ed) {
			//Log.d("API", ed.toString());
			fa.setTargetSearch(ed.toString());
			ArrayList<String> possibleTargets = new ArrayList<String>();
			for(String targetStr : targetList) {
				if(targetStr.toLowerCase().contains(ed.toString().toLowerCase()) 
						&& !targetStr.equals(StorageAPI.getInstance().getUsername(FriendsActivity.this))) {
					possibleTargets.add(targetStr);
				}
			}
			fa.setPossibleFriends(possibleTargets);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}
	}

	public class FriendsAdapter extends BaseAdapter implements OnItemClickListener, 
		OnItemLongClickListener {

		ArrayList<Friend> friends;
		ArrayList<String> possibleFriends;
		ArrayList<String> allList;
		boolean searchMode = false;
		String targetSearch;

		public FriendsAdapter(ArrayList<Friend> friends) {
			this.friends = friends;
			possibleFriends = new ArrayList<String>();
		}
		
		@Override
		public void notifyDataSetChanged() {
			sortFriendsList();
			super.notifyDataSetChanged();
		}
		
		public void sortFriendsList() {
			Collections.sort(friends, new Comparator<Friend>() {
				@Override
				public int compare(Friend lhs, Friend rhs) {
					return lhs.getTargetName().toUpperCase().compareTo(rhs.getTargetName().toUpperCase());
				}
			});
		}

		public void setFriends(ArrayList<Friend> friends) {
			this.friends = friends;
			notifyDataSetChanged();
		}

		public void setAllList(ArrayList<String> allList) {
			this.allList = allList;
			notifyDataSetChanged();
		}

		public void setTargetSearch(final String targetSearch) {
			this.targetSearch = targetSearch;
			Log.d("API", "targetSearch: "+targetSearch);
			Log.d("API", "possibleFriends: "+possibleFriends);
			if(allList.contains(targetSearch) || targetSearch.equals("")) {
				Log.d("API", "possibleFriends contains it");
				Handler mHandler = new Handler();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						((Button)findViewById(R.id.button_friends_add_apply)).setVisibility(View.GONE);
					}
				});
			}
			else {
				Log.d("API", "possibleFriends does not contain "+targetSearch);
				Log.d("API", "showing create new button");

				Handler mHandler = new Handler();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						((Button)findViewById(R.id.button_friends_add_apply)).setVisibility(View.VISIBLE);
						((Button)findViewById(R.id.button_friends_add_apply)).setText("Add "+targetSearch);
					}
				});


				((Button)findViewById(R.id.button_friends_add_apply)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String password = AppEngineAPI.getInstance().add(targetSearch);
						if(password!=null) {
							StorageAPI.getInstance().addFriend(FriendsActivity.this, targetSearch, password);
							toggleSearch();
							goToTarget(targetSearch, password);
						}
						else {
							Toast.makeText(FriendsActivity.this, "Error: Check your internet connection.", Toast.LENGTH_LONG).show();
						}

					}
				});
			}
			notifyDataSetChanged();
		}

		public void setPossibleFriends(ArrayList<String> possibleFriends) {
			this.possibleFriends = possibleFriends;

			notifyDataSetChanged();
		}

		public void toggleMode() {
			searchMode = !searchMode;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if(!searchMode)
				return friends.size();
			else
				return possibleFriends.size();
		}

		@Override
		public Object getItem(int position) {
			if(!searchMode)
				return friends.get(position);
			else
				return possibleFriends.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if(v==null) {
				if(!searchMode) {
					v = getLayoutInflater().inflate(R.layout.friend_item, null);
					v.setTag("target");
				}
				else {
					v = getLayoutInflater().inflate(R.layout.friend_item_search, null);
					v.setTag("search");
				}
			}

			if(!searchMode) {
				((Button)findViewById(R.id.button_friends_add_apply)).setVisibility(View.GONE);

				if(v.getTag().equals("search")) {
					v = getLayoutInflater().inflate(R.layout.friend_item, null);
					v.setTag("target");
				}

				((TextView)v.findViewById(R.id.text_friend_item))
				.setText(friends.get(position).getTargetName());
			}
			else {
				if(v.getTag().equals("target")) {
					v = getLayoutInflater().inflate(R.layout.friend_item_search, null);
					v.setTag("search");
				}

				((TextView)v.findViewById(R.id.text_friend_item_search))
				.setText(possibleFriends.get(position));
			}

			return v;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
			if(!searchMode) {
				goToTarget(friends.get(pos).getTargetName(), friends.get(pos).getPassword());
			}
			else {
				final String targetName = possibleFriends.get(pos);
				String password = null;
				for(Friend f : StorageAPI.getInstance().getFriends(FriendsActivity.this)) {
					if(f.getTargetName().equals(targetName)) {
						password = f.getPassword();
						break;
					}
				}

				if(password == null) {

					AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
					final View targetPasswordView = getLayoutInflater().inflate(R.layout.target_password, null);
					builder.setMessage("Enter password for "+targetName)
					.setCancelable(true)
					.setPositiveButton("Go", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String pwd = ((EditText) targetPasswordView.findViewById(R.id.edit_target_password)).getText().toString();
							if(AppEngineAPI.getInstance().login(targetName, pwd)==0) {
								StorageAPI.getInstance().addFriend(FriendsActivity.this, targetName, pwd);
								toggleSearch();
								goToTarget(targetName, pwd);
							}
							else {
								Toast.makeText(FriendsActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
							}
						}
					});

					builder.setView(targetPasswordView);
					AlertDialog alert = builder.create();
					alert.show();


				}
				else {
					toggleSearch();
					goToTarget(targetName, password);
				}
			}

		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				final int pos, long arg3) {
			AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
			builder.setMessage("Delete friend?")
			.setCancelable(true)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					StorageAPI.getInstance().deleteFriend(FriendsActivity.this, friends.get(pos).getTargetName());
					fa.setFriends(StorageAPI.getInstance().getFriends(FriendsActivity.this));
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return false;
		}

	}

	public void goToTarget(String targetName, String password) {
		Intent intent = new Intent(FriendsActivity.this, TargetActivity.class);
		intent.putExtra("targetName", targetName);
		intent.putExtra("password", password);
		startActivity(intent);
	}
	
	public void topLeftClicked(View v) {
		goToTarget(StorageAPI.getInstance().getUsername(FriendsActivity.this),
				StorageAPI.getInstance().getPassword(FriendsActivity.this));
	}
	
	public void topRightClicked(View v) {
		toggleSearch();
	}

	private class GetTargetListTask extends AsyncTask<Void, Void, Void> {

		FriendsTextWatcher ftw;
		FriendsAdapter fa;
		ArrayList<String> targetList;
		public GetTargetListTask(FriendsTextWatcher ftw, FriendsAdapter fa) {
			this.ftw = ftw;
			this.fa = fa;
		}

		protected Void doInBackground(Void... urls) {
			targetList = AppEngineAPI.getInstance().list();
			return null;
		}

		protected void onProgressUpdate(Void... progress) {
		}

		protected void onPostExecute(Void result) {
			ftw.setTargetList(targetList);
			fa.setAllList(targetList);
		}
	}

}