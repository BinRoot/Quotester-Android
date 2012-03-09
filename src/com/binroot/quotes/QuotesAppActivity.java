package com.binroot.quotes;

import API.AppEngineAPI;
import API.StorageAPI;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class QuotesAppActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if(StorageAPI.getInstance().isLoggedIn(this)) {
			((EditText)findViewById(R.id.edit_username)).setText(StorageAPI.getInstance().getUsername(this));
			((EditText)findViewById(R.id.edit_password)).setText(StorageAPI.getInstance().getPassword(this));
			startActivity(new Intent(this, FriendsActivity.class));
		}

		((EditText)findViewById(R.id.edit_username)).setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//Log.d("API", "actionId="+actionId+", "+EditorInfo.IME_ACTION_GO);
				if(actionId == EditorInfo.IME_ACTION_NEXT) {
					((EditText)findViewById(R.id.edit_password)).requestFocus();
					return true;
				}
				return false;
			}
		});
		
		((EditText)findViewById(R.id.edit_password)).setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//Log.d("API", "actionId="+actionId+", "+EditorInfo.IME_ACTION_GO);
				if(actionId == EditorInfo.IME_ACTION_GO) {
					attemptLogIn();
					return true;
				}
				return false;
			}
		});


		Button goButton = (Button) findViewById(R.id.button_go);
		goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptLogIn();
			}
		});

	}
	
	public void attemptLogIn() {
		String username = ((EditText)findViewById(R.id.edit_username)).getText().toString();
		String password = ((EditText)findViewById(R.id.edit_password)).getText().toString();
		if(AppEngineAPI.getInstance().login(username, password) == 0) {
			StorageAPI.getInstance().setLoggedIn(QuotesAppActivity.this, true);
			StorageAPI.getInstance().setUsername(QuotesAppActivity.this, username);
			StorageAPI.getInstance().setPassword(QuotesAppActivity.this, password);
			startActivity(new Intent(QuotesAppActivity.this, FriendsActivity.class));
		}
		else if(AppEngineAPI.getInstance().login(username, password) == 1) {
			Toast.makeText(QuotesAppActivity.this, "Creating new user...", Toast.LENGTH_SHORT).show();
			StorageAPI.getInstance().setLoggedIn(QuotesAppActivity.this, true);
			StorageAPI.getInstance().setUsername(QuotesAppActivity.this, username);
			StorageAPI.getInstance().setPassword(QuotesAppActivity.this, password);
			if(AppEngineAPI.getInstance().add(username, password)) {
				Toast.makeText(QuotesAppActivity.this, "Success!", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(QuotesAppActivity.this, FriendsActivity.class));
			}
			else Toast.makeText(QuotesAppActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(QuotesAppActivity.this, "Incorrect password. Try again or make a new account.", Toast.LENGTH_LONG).show();
		}
	}
}