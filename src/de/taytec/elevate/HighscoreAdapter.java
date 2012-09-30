/**
 * 
 */
package de.taytec.elevate;

import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author tay
 * 
 */
public class HighscoreAdapter extends ArrayAdapter<HighscoreItem> implements
		LoaderCallbacks<List<HighscoreItem>> {

	static final int MAXITEMS = 500;

	/**
	 * 
	 * @param context
	 */
	public HighscoreAdapter(Context context) {
		super(context, 0);
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
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater
					.inflate(R.layout.highscore_list_item, parent, false);
		} else {
			view = convertView;
		}

		TextView lineView = (TextView) view.findViewById(R.id.itemLine);
		TextView nameView = (TextView) view.findViewById(R.id.itemName);
		TextView valueView = (TextView) view.findViewById(R.id.itemValue);

		HighscoreItem item = getItem(position);
		String nick = item.getNick();
		CharSequence rank;
		if (nick.equals("~~RECORD~~")) {
			rank = "";
			nick = getContext().getResources().getString(R.string.record);
		} else {
			rank = String.valueOf(position);
		}
		lineView.setText(rank);
		nameView.setText(nick);
		valueView.setText(String.valueOf(item.getPoints()));

		return view;
	}

	/*
	 * Interface LoaderManager.LoaderCallbacks
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 * android.os.Bundle)
	 */
	@Override
	public Loader<List<HighscoreItem>> onCreateLoader(int arg0, Bundle arg1) {
		return new HighscoreLoader(getContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content
	 * .Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<List<HighscoreItem>> loader,
			List<HighscoreItem> data) {
		int idx = 0;
		ListIterator<HighscoreItem> i = data.listIterator();
		while (i.hasNext()) {
			HighscoreItem dItem = i.next();
			if (idx >= getCount()) {
				add(dItem);
				idx++;
				continue;
			}
			HighscoreItem aItem = getItem(idx);
			if (dItem.equals(aItem)) {
				idx++;
			}
			else if (dItem.isGreaterThan(aItem)) {
				insert(dItem, idx++);
			}
			else {
				for (int pos = getCount() - 1; pos >= idx;  pos--) {
					remove(getItem(pos));
				}
				add(dItem);
				idx++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.content
	 * .Loader)
	 */
	@Override
	public void onLoaderReset(Loader<List<HighscoreItem>> arg0) {
		clear();
	}

}
