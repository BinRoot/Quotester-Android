package com.binroot.quotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import API.AppEngineAPI;
import API.StorageAPI;
import Model.Quote;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TargetActivity extends Activity {
	
	String targetName = null;
	String password = null;
	TargetAdapter ta;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.target);

		Bundle b = getIntent().getExtras();
		targetName = b.getString("targetName");
		password = b.getString("password");

		ListView lv = (ListView) findViewById(R.id.list_target);
		ta = new TargetAdapter(targetName, password);
		lv.setAdapter(ta);
		lv.setOnItemLongClickListener(ta);
		lv.setOnItemClickListener(ta);
		
		new GetQuotesTask(targetName, password, ta).execute();
		
		((Button)findViewById(R.id.button_quote_add))
			.setOnClickListener(new AddQuoteClickListener(ta, targetName, password));
		
		((Button)findViewById(R.id.button_target_back))
			.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					TargetActivity.this.finish();
				}
			});
		

		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/SLICKER.TTF");
		
		((TextView)findViewById(R.id.text_target_name)).setText(targetName);
		((TextView)findViewById(R.id.text_target_name)).setTypeface(tf);
		
		((TextView)findViewById(R.id.text_target_pwd)).setText(password);
		((TextView)findViewById(R.id.text_target_pwd)).setTypeface(tf);
		
		((ImageButton)findViewById(R.id.button_target_help)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showHelpDialog();
			}
		});
	}
	
	public class AddQuoteClickListener implements OnClickListener {

		TargetAdapter ta;
		String targetName;
		String password;
		
		public AddQuoteClickListener(TargetAdapter ta, String targetName, String password) {
			this.ta = ta;
			this.targetName = targetName;
			this.password = password;
		}
		
		@Override
		public void onClick(View v) {
			showQuoteDialog(targetName, password, ta);
		}
	}

	public class TargetAdapter extends BaseAdapter implements OnItemLongClickListener, OnItemClickListener {

		ArrayList<Quote> quotes;
		String targetName;
		String password;
		
		public TargetAdapter(String targetName, String password) {
			quotes = new ArrayList<Quote>();
			this.targetName = targetName;
			this.password = password;
		}
		
		@Override
		public void notifyDataSetChanged() {
			sortQuotesByDate();
			super.notifyDataSetChanged();
		}
		
		public void sortQuotesByDate() {
			Collections.sort(quotes, new Comparator<Quote>() {
				@Override
				public int compare(Quote q1, Quote q2) {
					if(q1.getDate() != null && q2.getDate() != null) {
						if(q1.getDate().after(q2.getDate())) {
							return -1;
						}
						else if(q1.getDate().before(q2.getDate())) {
							return 1;
						}
						else return 0;
					}
					else return 0;
				}
			});
		}
		
		public void setQuotes(ArrayList<Quote> quotes) {
			this.quotes = quotes;
		}

		@Override
		public int getCount() {
			return quotes.size();
		}

		@Override
		public Quote getItem(int pos) {
			return quotes.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup arg2) {
			View v = convertView;
			if(v==null) {
				v = getLayoutInflater().inflate(R.layout.target_item, null);
				final Animation a = AnimationUtils.loadAnimation(TargetActivity.this, R.anim.translate);
				a.reset();
				v.setAnimation(a);
			}

			Quote q = getItem(pos);
			
			Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Junction.otf");
			((TextView)v.findViewById(R.id.text_target_item_content)).setTypeface(tf);

			((TextView)v.findViewById(R.id.text_target_item_content))
			.setText(q.getContent());

			((TextView)v.findViewById(R.id.text_target_item_author))
			.setText(q.getAuthor());

			String dateStr = "";
			if(q.getDate() != null) {
				dateStr = q.getDate().toString();
				String [] dateStrArr = dateStr.split(" ");
				dateStr = dateStrArr[0]+" "+dateStrArr[1]+" "+dateStrArr[2];
			}
			((TextView)v.findViewById(R.id.text_target_item_date)).setText(dateStr);

			
			
			return v;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				final int pos, long arg3) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(TargetActivity.this);
			builder.setMessage("Delete quote?")
			       .setCancelable(true)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   AppEngineAPI.getInstance().delete(targetName, password, getItem(pos).getName());
			        	   new GetQuotesTask(targetName, password, TargetAdapter.this).execute();
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

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, quotes.get(pos).getContent());

			startActivity(Intent.createChooser(shareIntent, "Share with friends"));
		}

	}
	
	public void showQuoteDialog(final String targetName, final String password, final TargetAdapter ta) {
		AlertDialog.Builder builder = new AlertDialog.Builder(TargetActivity.this);
		final View targetQuoteView = getLayoutInflater().inflate(R.layout.target_quote, null);
		builder.setMessage("Enter quote")
		       .setCancelable(true)
		       .setPositiveButton("Done", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   String quote = ((EditText) targetQuoteView
		        			   .findViewById(R.id.edit_target_quote)).getText().toString();
		        	   if(AppEngineAPI.getInstance().post(targetName, password, quote, 
		        			   StorageAPI.getInstance().getUsername(TargetActivity.this)) ) {
		        		   Toast.makeText(TargetActivity.this, "Success!", Toast.LENGTH_SHORT).show();
		        		   new GetQuotesTask(targetName, password, ta).execute();
		        	   }
		        	   else {
		        		   Toast.makeText(TargetActivity.this, "Error: Check your internet connection.", Toast.LENGTH_LONG).show();
		        	   }
		        	   
		           }
		       });
		
		builder.setView(targetQuoteView);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(TargetActivity.this);
		final View targetHelpView = getLayoutInflater().inflate(R.layout.target_help, null);
		

		Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Junction.otf");
		((TextView)targetHelpView.findViewById(R.id.text_target_help)).setTypeface(tf);
		builder.setTitle("What's this password?")
		       .setCancelable(true)
		       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		builder.setIcon(android.R.drawable.ic_menu_help);
		builder.setView(targetHelpView);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void topRightClicked(View v) {
		// do what button does
		if(targetName!=null && password!=null && ta!=null) {
			showQuoteDialog(targetName, password, ta);
		}
	}
	
	public void topLeftClicked(View v) {
		// do what button does
		TargetActivity.this.finish();
	}

	private class GetQuotesTask extends AsyncTask<Void, Void, Void> {
		String targetName;
		String password;
		TargetAdapter ta;
		
		@Override
		public void onPreExecute() {
			ta.setQuotes(StorageAPI.getInstance().getSavedQuotes(TargetActivity.this, targetName));
			ta.notifyDataSetChanged();
		}
		
		public GetQuotesTask(String targetName, String password, TargetAdapter ta) {
			this.targetName = targetName;
			this.password = password;
			this.ta = ta;
		}
		
		protected Void doInBackground(Void... urls) {
			ArrayList<Quote> quotesFromServer = AppEngineAPI.getInstance().quotes(targetName, password);
			if(quotesFromServer != null) {
				ta.setQuotes(AppEngineAPI.getInstance().quotes(targetName, password));
			}
			return null;
		}

		protected void onProgressUpdate(Void... progress) {
		}

		protected void onPostExecute(Void result) {
			StorageAPI.getInstance().saveQuotes(TargetActivity.this, targetName, ta.quotes);
			ta.notifyDataSetChanged();
		}
	}
}
