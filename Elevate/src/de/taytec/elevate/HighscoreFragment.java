package de.taytec.elevate;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HighscoreFragment extends ListFragment {

	private int lastScore;
	private View mView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setListAdapter(((ElevateActivity) getActivity())
				.getHighscoreListAdapter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater
	 * , android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "onCreateView " + container);
		if (null != mView && mView.getParent() instanceof ViewGroup) {
			((ViewGroup) mView.getParent()).removeView(mView);
		} else {
			mView = inflater.inflate(R.layout.highscore_dialog, container,
					false);
			TextView tvScoreValue = (TextView) mView
					.findViewById(R.id.tvScoreValue);
			tvScoreValue.setText(String
					.valueOf(lastScore > 0 ? lastScore : "-"));
		}
		return mView;
	}

	/**
	 * 
	 * @param score
	 */
	public void setScore(int score) {
		if (score > lastScore) {
			lastScore = score;
		}
		if (null != getView()) {
			TextView tvScoreValue = (TextView) getView().findViewById(
					R.id.tvScoreValue);
			tvScoreValue.setText(String
					.valueOf(lastScore > 0 ? lastScore : "-"));
		}
	}

}
