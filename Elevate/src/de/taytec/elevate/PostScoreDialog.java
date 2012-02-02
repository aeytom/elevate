package de.taytec.elevate;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PostScoreDialog extends DialogFragment {

	private Context mContext;


	public PostScoreDialog(Context context) {
		mContext = context;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final Bundle params = new Bundle();
		params.putInt("score", savedInstanceState.getInt("score"));

		final View view = inflater.inflate(R.layout.post_hiscore, container);
		Button btnPost = (Button) view.findViewById(R.id.btnPost);
		btnPost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText etNick = (EditText) view.findViewById(R.id.etNick);
				Editable nick = etNick.getText();
				params.putCharSequence("nick", nick);
				new HighscorePostTask(mContext).execute(params);
				dismiss();
			}
		});
		return view;
	}
}
