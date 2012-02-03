package de.taytec.elevate;

import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public class HighscorePostTask extends AsyncTask<Bundle, Integer, Boolean> {

	private Context mContext;

	public HighscorePostTask(Context context) {
		mContext = context;
	}

	/**
	 * @return the Context
	 */
	public Context getContext() {
		return mContext;
	}

	@Override
	protected Boolean doInBackground(Bundle... params) {

		String score = String.valueOf(params[0].getInt("score"));

		ArrayList<NameValuePair> vals = new ArrayList<NameValuePair>(4);
		vals.add(new BasicNameValuePair("nick", params[0].getString("nick")));
		vals.add(new BasicNameValuePair("result", score));

		HttpResponse response;
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(getContext().getResources()
					.getString(R.string.hiscoreApiKey).getBytes("UTF8"));
			digester.update(String.valueOf(score).getBytes("UTF8"));
			vals.add(new BasicNameValuePair("check", Base64.encodeToString(
					digester.digest(), Base64.NO_WRAP)));
			vals.add(new BasicNameValuePair("app", "de.taytec.elevate"));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 2000);
			HttpPost postMethod = new HttpPost(getContext().getResources()
					.getString(R.string.hiscoreJsonUrl));
			postMethod.setEntity(new UrlEncodedFormEntity(vals));
			response = httpClient.execute(postMethod);
		} catch (Exception e) {
			if (ElevateActivity.DEBUG) Log.e(getClass().getSimpleName(), e.getMessage());
			return false;
		}

		return 200 == response.getStatusLine().getStatusCode();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "onPostExecute()"+result);
		((ElevateActivity) getContext()).refreshHighscore();
	}
	
	

}
