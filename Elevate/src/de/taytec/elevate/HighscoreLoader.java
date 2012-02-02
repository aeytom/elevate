package de.taytec.elevate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * 
 * @author tay
 * 
 */
class HighscoreLoader extends AsyncTaskLoader<List<HighscoreItem>> {

	private int lowestHiscore = -1;

	/**
	 * 
	 * @param context
	 */
	public HighscoreLoader(Context context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see android.content.Loader#onStartLoading()
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	/**
	 * loads current highscore list from web server
	 */
	@Override
	public List<HighscoreItem> loadInBackground() {
		Log.d(getClass().getSimpleName(), "loadInBackground()");
		ArrayList<HighscoreItem> list = new ArrayList<HighscoreItem>();
		lowestHiscore = -1;

		URL url = null;
		HttpsURLConnection con = null;
		String content = "";
		try {
			url = new URL(getContext().getResources().getString(
					R.string.hiscoreJsonUrl));
			con = (HttpsURLConnection) url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(2000);
			if (HttpURLConnection.HTTP_OK != con.getResponseCode()) {
				Log.e(getClass().getSimpleName(),
						"Response code: " + con.getResponseMessage());
				return list;
			}
			content = convertStreamToString(con.getInputStream());

		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), getContext().getResources()
					.getString(R.string.hiscoreJsonUrl) + ": " + e.getMessage());
			return list;
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.toString());
			return list;
		} finally {
			if (null != con) {
				con.disconnect();
			}
		}

		try {
			JSONArray ja = new JSONArray(content);
			JSONObject jo = null;
			for (int i = 0; i < ja.length() && i < HighscoreAdapter.MAXITEMS; i++) {
				jo = ja.getJSONObject(i);
				list.add(new HighscoreItem(jo));
			}
			lowestHiscore = (null == jo) ? 0 : jo.getInt("points");
		} catch (JSONException e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		}

		return list;
	}

	/**
	 * get the minimal highscore
	 * 
	 * @return
	 */
	int getMinimumHighscore() {
		return lowestHiscore;
	}

	/**
	 * 
	 * @param is
	 * @return
	 */
	private String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				16384);
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

}