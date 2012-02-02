package de.taytec.elevate;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		String versionName = "";
		try {
			PackageInfo pInfo = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(),
							PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(getActivity().getPackageName(),
					"createAboutDialog() : PackageManager.GET_META_DATA", e);
		}
		Log.v(getClass().getSimpleName(), "onCreateView "+container);
		View view = inflater.inflate(R.layout.about, container, false);
		TextView tvAbout = (TextView) view.findViewById(R.id.tvAbout);
		tvAbout.setText(Html.fromHtml(getResources().getString(R.string.about).replace("VERSION", versionName)));
		return view;
	}

}
