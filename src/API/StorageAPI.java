package API;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.Friend;
import Model.Quote;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class StorageAPI {
	private static StorageAPI storageAPI;

	public static StorageAPI getInstance() {
		if (storageAPI == null) {
			storageAPI = new StorageAPI();
		}
		return storageAPI;
	}
	
	public boolean isLoggedIn(Activity act) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        return settings.getBoolean("loggedin", false);
	}
	
	public void setLoggedIn(Activity act, boolean val) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        settings.edit().putBoolean("loggedin", val).commit();
	}
	
	public String getUsername(Activity act) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        return settings.getString("user", null);
	}
	
	public void setUsername(Activity act, String name) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        settings.edit().putString("user", name).commit();
	}
	
	public String getPassword(Activity act) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        return settings.getString("password", null);
	}
	
	public void setPassword(Activity act, String name) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        settings.edit().putString("password", name).commit();
	}
	
	public void addFriend(Activity act, String targetName, String password) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        String friends = settings.getString("friends", "{\"data\": []}");
        
        try {
        	
			JSONObject jo = new JSONObject(friends);
			
			boolean contains = false;
			JSONArray ja = jo.getJSONArray("data");
			for(int i=0; i<ja.length(); i++) {
				JSONObject fObj = ja.getJSONObject(i);
				if(fObj.get("targetName").equals(targetName)) {
					contains = true;
				}
			}
			
			if(!contains) {
				JSONObject friendJSONObject = new JSONObject();
				friendJSONObject.put("targetName", targetName);
				friendJSONObject.put("password", password);
				jo.getJSONArray("data").put(friendJSONObject);
				System.out.println("json: "+jo.toString());
				settings.edit().putString("friends", jo.toString()).commit();
			}
			else {
				System.out.println("storage already contains that friend");
			}
			
		} catch (JSONException e) {
			System.out.println("error: "+e.getMessage());
		}
	}
	
	public void deleteFriend(Activity act, String targetName) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        String friends = settings.getString("friends", "{\"data\": []}");
        
        try {
        	
			JSONObject jo = new JSONObject(friends);
			
			boolean contains = false;
			JSONArray ja = jo.getJSONArray("data");
			JSONArray ja2 = new JSONArray();
			for(int i=0; i<ja.length(); i++) {
				JSONObject fObj = ja.getJSONObject(i);
				if(fObj.get("targetName").equals(targetName)) {
					continue;
				}
				else {
					ja2.put(fObj);
				}
			}
			
			JSONObject jo2 = new JSONObject();
			jo2.put("data", ja2);
			settings.edit().putString("friends", jo2.toString()).commit();
			System.out.println("storage: "+jo2.toString());
			
		} catch (JSONException e) {
			System.out.println("error: "+e.getMessage());
		}
	}
	
	public ArrayList<Friend> getFriends(Activity act) {
		SharedPreferences settings = act.getSharedPreferences("account", 0);
        String friends = settings.getString("friends", "{\"data\": []}");
        
        ArrayList<Friend> friendsList = new ArrayList<Friend>();
        try {
			JSONObject jo = new JSONObject(friends);
			
			boolean contains = false;
			JSONArray ja = jo.getJSONArray("data");
			for(int i=0; i<ja.length(); i++) {
				JSONObject fObj = ja.getJSONObject(i);
				
				friendsList.add(new Friend(fObj.getString("targetName"), 
						fObj.getString("password")));
			}
			
		} catch (JSONException e) {
			System.out.println("error: "+e.getMessage());
		}
        
        return friendsList;
	}
	
	public ArrayList<Quote> getSavedQuotes(Activity act, String targetName) {
		SharedPreferences settings = act.getSharedPreferences("quotes", 0);
        String quotesStr = settings.getString(targetName, "{\"data\": []}");
        
        ArrayList<Quote> quoteList = new ArrayList<Quote>();
        try {
			JSONObject jo = new JSONObject(quotesStr);
			
			JSONArray ja = jo.getJSONArray("data");
			for(int i=0; i<ja.length(); i++) {
				JSONObject fObj = ja.getJSONObject(i);
				
				Quote q = new Quote();
				q.setAuthor(fObj.getString("author"));
				q.setContent(fObj.getString("content"));
				
				Log.d("API", "Trying to parse "+fObj.getString("date"));
				String dateStr = fObj.getString("date").toString();
				
				//SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
				SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz");
				try {
					q.setDate(df.parse(dateStr));
				} catch (ParseException e) {
					Log.d("API", "error*: "+e.getMessage());
				}
				
				q.setName(fObj.getString("name"));
				
				if(fObj.getString("points")!=null && !fObj.getString("points").equals("null")) {
					q.setPoints(Integer.parseInt(fObj.getString("points")));
				}
				else {
					q.setPoints(0);
				}
				
				q.setUser(fObj.getString("user"));
				
				quoteList.add(q);
			}
			
		} catch (JSONException e) {
			System.out.println("error: "+e.getMessage());
		}
        
        return quoteList;
	}
	
	public void saveQuotes(Activity act, String targetName, ArrayList<Quote> quotesList) {
		SharedPreferences settings = act.getSharedPreferences("quotes", 0);
        
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
        for(Quote q : quotesList) {
        	JSONObject jo2 = new JSONObject();
        	try {
        		
        		Log.d("API", "flattening to JSON: "+q.toString());
        		
				jo2.accumulate("author", q.getAuthor());
				jo2.accumulate("content", q.getContent());
				jo2.accumulate("date", q.getDate().toGMTString());
				jo2.accumulate("name", q.getName());
				jo2.accumulate("points", q.getPoints());
				jo2.accumulate("user", q.getUser());
			} catch (JSONException e) {
				Log.d("API", "ERR: "+e.getMessage());
				e.printStackTrace();
			}
        	
        	ja.put(jo2);
        }
        try {
			jo.accumulate("data", ja);
		} catch (JSONException e) {
			Log.d("API", "ERR: "+e.getMessage());
			e.printStackTrace();
		}
        
        settings.edit().putString(targetName, jo.toString()).commit();
	}
}
