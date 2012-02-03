package de.taytec.elevate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	private View mView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "onCreateView " + container);
		if (null != mView && mView.getParent() instanceof ViewGroup) {
			((ViewGroup) mView.getParent()).removeView(mView);
		} else {
			mView = inflater.inflate(R.layout.about, container, false);
			TextView tvAbout = (TextView) mView.findViewById(R.id.tvAbout);
			tvAbout.setText(Html.fromHtml(getResources().getString(
					R.string.about)));
		}
		return mView;
	}

}
