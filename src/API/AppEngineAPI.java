package API;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.Quote;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

public class AppEngineAPI {
	private static AppEngineAPI appEngineAPI;

	public static AppEngineAPI getInstance() {
		if (appEngineAPI == null) {
			appEngineAPI = new AppEngineAPI();
		}
		return appEngineAPI;
	}
	
	
	public ArrayList<String> list() {
		String listStr = getData("http://io-storage.appspot.com/list");
		Log.d("API", "list: "+listStr);
		
		// return an ArrayList<String> targetNames
		String targetArr[] = listStr.split(";");
		ArrayList<String> retList = new ArrayList<String>(Arrays.asList(targetArr));
		return retList ;
	}
	
	public int login(String username, String password) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("username", username);
		attrMap.put("password", password);
		
		String result = postData("http://io-storage.appspot.com/login", attrMap);
		Log.d("API", "result: "+result);
		if(result!=null) {
			return Integer.parseInt(result.split(" ")[0]);
		}
		else return 2;
	}
	
	public boolean add(String targetName, String password) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		attrMap.put("password", password);
		
		String result = postData("http://io-storage.appspot.com/add", attrMap);
		Log.d("API", "result: "+result);
		
		if(result!=null) {
			if(result.split(" ")[0].equals("0")) {
				return true;
			}
		}
		return false;
	}
	
	public String add(String targetName) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		
		String result = postData("http://io-storage.appspot.com/add", attrMap);
		Log.d("API", "result: "+result);
		if(result != null) {
			if(result.split(" ")[0].equals("0")) {
				try {
					String outStr = result.split(";")[1];
					return outStr;
				}
				catch(ArrayIndexOutOfBoundsException e) {
					return null;
				}
			}
			else return null;
		}
		else return null;
		
	}
	
	public boolean post(String targetName, String password, String content) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		attrMap.put("password", password);
		attrMap.put("content", content);
		
		String result = postData("http://io-storage.appspot.com/post", attrMap);
		Log.d("API", "result: "+result);
		
		if(result != null) {
			if(result.split(" ")[0].equals("0")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean post(String targetName, String password, String content, String author) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		attrMap.put("password", password);
		attrMap.put("content", content);
		attrMap.put("author", author);
		
		String result = postData("http://io-storage.appspot.com/post", attrMap);
		Log.d("API", "result: "+result);
		
		if(result != null) {
			if(result.split(" ")[0].equals("0")) {
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	public ArrayList<Quote> quotes(String targetName, String password) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		attrMap.put("password", password);
		
		String result = postData("http://io-storage.appspot.com/quotes", attrMap);
		Log.d("API", "result: "+result);
		
		ArrayList<Quote> quotes = null;
		try {
			JSONObject jo = new JSONObject(result);
			JSONArray ja = jo.getJSONArray("data");
			quotes = new ArrayList<Quote>();
			for(int i=0; i<ja.length(); i++) {
				JSONObject quoteJSONObject = ja.getJSONObject(i);
				Quote q = new Quote();
				q.setAuthor(quoteJSONObject.getString("author"));
				q.setContent(quoteJSONObject.getString("content"));
					
				String dateStr = quoteJSONObject.getString("date");
				dateStr = dateStr.replace("UTC ", "");
				Log.d("API", "parsing date:"+dateStr+";");
				SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
				
				
				try {
					Date utcDate = df.parse(dateStr);
					int offset = Calendar.getInstance().getTimeZone().getRawOffset();
					
					utcDate.setTime(utcDate.getTime()+offset);
					
					q.setDate(utcDate);
				} catch (ParseException e) {
					Log.d("API", "error from AppEngine: "+e.getMessage());
				}

				q.setName(quoteJSONObject.getString("name"));
				
				if(quoteJSONObject.getString("points")!=null && !quoteJSONObject.getString("points").equals("null")) {
					q.setPoints(quoteJSONObject.getInt("points"));
				}
				else {
					q.setPoints(0);
				}
				
				q.setUser(quoteJSONObject.getString("user"));

				quotes.add(q);
			}
		} catch (JSONException e) {
			Log.d("API", "error: "+e.getMessage());
		}
		catch (NullPointerException e) {
			Log.d("API", "null pointer err "+e.getMessage());
		}
		return quotes;
	}
	
	public boolean delete(String targetName, String password, String name) {
		HashMap<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("targetName", targetName);
		attrMap.put("password", password);
		attrMap.put("name", name);
	
		String result = postData("http://io-storage.appspot.com/delete", attrMap);
		Log.d("API", "result: "+result);
		
		if(result.split(" ")[0].equals("0")) {
			return true;
		}
		else return false;
	}

	
	public String postData(String url, HashMap<String, String> attributes) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(attributes.size());
	        for(String key : attributes.keySet()) {
	        	nameValuePairs.add(new BasicNameValuePair(key, attributes.get(key)));
	        }
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        String responseBody = EntityUtils.toString(response.getEntity());
	        return responseBody;
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    
	    return null;
	} 
	
	
	private String getData(String urlStr)
	{
		
	    InputStream is = null;
	    try 
	    {
	    	URL connectURL = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
			
	        is = conn.getInputStream(); 
	        // scoop up the reply from the server
	        int ch; 
	        StringBuffer sb = new StringBuffer(); 
	        while( ( ch = is.read() ) != -1 ) { 
	            sb.append( (char)ch ); 
	        } 
	        return sb.toString(); 
	    }
	    catch(Exception e)
	    {
	       Log.e("API", "biffed it getting HTTPResponse");
	    }
	    finally 
	    {
	        try {
	        if (is != null)
	            is.close();
	        } catch (Exception e) {}
	    }

	    return "";
	}

}
