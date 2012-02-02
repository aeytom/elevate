package de.taytec.elevate;

import org.json.JSONException;
import org.json.JSONObject;

public class HighscoreItem {
	String mNick;
	int mPoints;
	
	/**
	 * @param nick
	 * @param points
	 * @throws JSONException 
	 */
	public HighscoreItem(JSONObject json) throws JSONException {
		mNick = json.getString("nick");
		try {
			mPoints = json.getInt("points");
		}
		catch (JSONException e) {
			mPoints = Integer.parseInt(json.getString("points"));
		}
	}

	/**
	 * @return the nick
	 */
	public String getNick() {
		return mNick;
	}
	
	/**
	 * @return the points
	 */
	public int getPoints() {
		return mPoints;
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public boolean equals(HighscoreItem o)
	{
		return (getPoints() == o.getPoints() && getNick().equals(o.getNick()));
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public boolean isGreaterThan(HighscoreItem o)
	{
		return (getPoints() > o.getPoints())
				|| (getPoints() == o.getPoints() && ! getNick().equals(o.getNick()));
	}
}
