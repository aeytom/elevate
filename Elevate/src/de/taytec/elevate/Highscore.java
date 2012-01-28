/**
 * 
 */
package de.taytec.elevate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author tay
 * 
 */
public class Highscore extends ArrayAdapter<JSONObject> {
	private static final int MAXITEMS = 50;
	private int lowestHiscore = -1;



	/**
	 * 
	 * @param context
	 */
	public Highscore(Context context) {
		super(context, 0);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean setupScoreData() {
		URL url = null;
		HttpsURLConnection con = null;
		String content = "";
		try {
			url = new URL(getContext().getResources().getString(R.string.hiscoreJsonUrl));
			con = (HttpsURLConnection) url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(2000);
			if (HttpURLConnection.HTTP_OK != con.getResponseCode()) {
				Log.e("Elevate::Highscore", "Response code: "+con.getResponseMessage());
				return false;
			}
			content = convertStreamToString(con.getInputStream());
			
		} 
		catch (MalformedURLException e) {
			Log.e("Elevate:Highscore", getContext().getResources().getString(R.string.hiscoreJsonUrl) + ": " + e.getMessage());
			return false;
		} 
		catch (Exception e) {
			Log.e("Elevate:Highscore", e.getMessage());
			return false;
		} 
		finally {
			if (null != con) {
				con.disconnect();
			}
		}

		try {
			clear();
			JSONArray ja = new JSONArray(content);
			JSONObject jo = null;
			for (int i=0; i<ja.length() && i < MAXITEMS; i++) {
				jo = ja.getJSONObject(i);
				add(jo);
			}
			setLowestHiscore(jo.getInt("points"));
		} 
		catch (JSONException e) {
			Log.e("Elevate:Highscore", e.getMessage());
			return false;
		}
		
		notifyDataSetChanged();

		return true;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.CursorAdapter#newView(android.content.Context,
	 * android.database.Cursor, android.view.ViewGroup)
	 */
    public View getView(int position, View convertView, ViewGroup parent) {
    	View view;
    	if (null == convertView) {
    		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		view = inflater.inflate(R.layout.highscore_list_item, parent, false);
    	}
    	else {
    		view = convertView;
    	}		

    	TextView lineView = (TextView) view.findViewById(R.id.itemLine);
    	TextView nameView = (TextView) view.findViewById(R.id.itemName);
    	TextView valueView = (TextView) view.findViewById(R.id.itemValue);

    	JSONObject item = getItem(position);
    	try {
    		String nick = item.getString("nick");
    		CharSequence rank;
			if (nick.equals("~~RECORD~~")) {
    			rank = "";
    			nick = getContext().getResources().getString(R.string.record);
    		}
    		else {
    			rank = String.valueOf(position);
    		}
    		lineView.setText(rank);
			nameView.setText(nick);
	    	valueView.setText(item.getString("points"));
		} 
    	catch (JSONException e) {
			Log.e("Elevate:Highscore", e.getMessage());
		}

    	return view;
    }

    /**
     * 
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 16384);
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


	/**
	 * 
	 * @param nick
	 * @return
	 */
	public boolean postScore(CharSequence nick, int score) {
		ArrayList<NameValuePair> vals = new ArrayList<NameValuePair>(4);
		vals.add(new BasicNameValuePair("nick", nick.toString()));
		vals.add(new BasicNameValuePair("result", String.valueOf(score)));

		HttpResponse response;
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(getContext().getResources().getString(R.string.hiscoreApiKey).getBytes("UTF8"));
			digester.update(String.valueOf(score).getBytes("UTF8"));
			vals.add(new BasicNameValuePair("check", Base64.encodeToString(digester.digest(), Base64.NO_WRAP)));
			vals.add(new BasicNameValuePair("app", "de.taytec.elevate"));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
			HttpPost postMethod = new HttpPost(getContext().getResources().getString(R.string.hiscoreJsonUrl));
			postMethod.setEntity(new UrlEncodedFormEntity(vals));
			response = httpClient.execute(postMethod);
		}
		catch (Exception e) {
			Log.e("Elevate::Highscore", e.getMessage());
			return false;
		}
		
		return 200 == response.getStatusLine().getStatusCode();
	}

	/**
	 * @param lowestHiscore the lowestHiscore to set
	 */
	protected void setLowestHiscore(int lowestHiscore) {
		this.lowestHiscore = lowestHiscore;
	}

	/**
	 * @return the lowestHiscore
	 */
	public int getLowestHiscore() {
		if (-1 == lowestHiscore) {
			setupScoreData();
		}
		return lowestHiscore;
	}
}
