package de.taytec.elevate;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

public class ElevateActivity extends FragmentActivity {

	static final boolean DEBUG = true;

	private FrameLayout detailPane;

	private HighscoreFragment mHighscore;

	private HighscoreAdapter mHighscoreAdapter;

	private ViewPager mPager;

	private Elevate mElevate;

	private AboutFragment mAbout;
	private static float drawableScaleFactor;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if (isTablet()) {
			if (metrics.widthPixels > metrics.heightPixels) {
				drawableScaleFactor = metrics.density * metrics.widthPixels / 1280F;
			}
			else {
				drawableScaleFactor = metrics.density;
			}
		} 
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			drawableScaleFactor = 0.75F * metrics.widthPixels / metrics.density / (146F + 86F + 146F);
		}
		
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), getWindowManager().getDefaultDisplay().getOrientation() + " :::: "
				+ ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), metrics.toString());
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "scale factor: " + drawableScaleFactor);

		
		mHighscoreAdapter = new HighscoreAdapter(this);
		refreshHighscore();
		mHighscore = new HighscoreFragment();


		if (isTablet()) {
			setContentView(R.layout.tablet);
			mElevate = (Elevate) getSupportFragmentManager().findFragmentById(R.id.game);
			mAbout = new AboutFragment();
			
			detailPane = (FrameLayout) findViewById(R.id.detailPane);
			if (null != detailPane && detailPane.getVisibility() != View.VISIBLE) {
				detailPane = null;
			}
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "detailPane: "+detailPane);
			onClickHelp(detailPane);
		}
		else {			
			setContentView(R.layout.handheld);
			mElevate = new Elevate();
			mAbout = new AboutFragment();
			ElevatePagerAdapter pagerAdapter = new ElevatePagerAdapter(getSupportFragmentManager());
			mPager = (ViewPager)findViewById(R.id.pager);
	        mPager.setAdapter(pagerAdapter);
		}
		
	}
	    
	/**
	 * 
	 * @param v
	 */
	public void onClickHighscore(View v) {
		showHighscore(0);
	}

	/**
	 * 
	 * @param score
	 */
	public void showHighscore(int score) {
		if (isTablet()) {
			if (detailPane != null) {
				if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "showHighscore() inline score="+score);
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.detailPane);				
				if (!(fragment instanceof HighscoreFragment)) {
					if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "showHighscore() replace inline");
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.detailPane, mHighscore, "highscore")
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
							.commit();
				}
			}			
		}
		else {
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "showHighscore() dialog");
			if (null != mPager) {
				mPager.setCurrentItem(1, true);
			}
		} 
		mHighscore.setScore(score);
		HighscoreLoader loader = (HighscoreLoader) getSupportLoaderManager().initLoader(0, null, mHighscoreAdapter);
		if (500 < score && score > loader.getMinimumHighscore()) {
			postScore(score);
		}
		refreshHighscore();
	}
	
	
	/**
	 * 
	 * @param score
	 */
	public void postScore(int score) {
		String tag = "PostScore";

		Bundle args = new Bundle();
		args.putInt("score", score);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		PostScoreDialog dialog = (PostScoreDialog) getSupportFragmentManager().findFragmentByTag(tag);
		if (null == dialog) {
			dialog = new PostScoreDialog(this);
		}

		ft.addToBackStack(null);
		dialog.setArguments(args);
		dialog.show(ft, tag);
	}

	/**
	 * 
	 */
	public void refreshHighscore() {
		HighscoreLoader loader = (HighscoreLoader) getSupportLoaderManager().initLoader(0, null, mHighscoreAdapter);
		loader.startLoading();
	}

	/**
	 * 
	 * @param v
	 */
	public void onClickHelp(View v) {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "show help");
		if (isTablet()) {
			if (detailPane != null) {
				if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "show help in detail pane");
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.detailPane);
				if (!(fragment instanceof AboutFragment)) {
					if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "about() replace inline");
					getSupportFragmentManager()
							.beginTransaction()
							.addToBackStack(null)
							.replace(R.id.detailPane, mAbout, "about")
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
							.commit();
				}
			}
		}
		else {
			if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "show help");
			if (null != mPager) {
				mPager.setCurrentItem(2, true);
			}
		}
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void onClickPlay(View v) {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "start game");
		if (null != mPager) {
			if (0 == mPager.getCurrentItem())
			{
				mElevate.startGame();			
			}
			else {
				mPager.setCurrentItem(0, true);
			}
		}
		else {
			mElevate.startGame();
		}
	}


	/**
	 * 
	 * @return
	 */
	public boolean haveNetworking() {
		String service = Context.CONNECTIVITY_SERVICE;
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(service);
		NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
		if (null == netInfo || !netInfo.isAvailable()) {
			// if (ElevateActivity.DEBUG) Log.d("Elevate", "Network not available");
			return false;
		}
		return true;
	}

	// DisplayMetrics{density=1.5, width=480, height=800, scaledDensity=1.5,
	// xdpi=240.0, ydpi=240.0}
	// DisplayMetrics{density=0.75, width=240, height=320, scaledDensity=0.75,
	// xdpi=120.0, ydpi=120.0}
	// DisplayMetrics{density=1.0, width=800, height=1280, scaledDensity=1.0,
	// xdpi=165.03554, ydpi=165.20325}

	public static int getScaledWidth(Drawable drawable) {
		return (int) (getScaleFactor() * drawable.getIntrinsicWidth());
	}

	/**
	 * 
	 * @param drawable
	 * @return
	 */
	public static int getScaledHeight(Drawable drawable) {
		return (int) (getScaleFactor() * drawable.getIntrinsicHeight());
	}

	/**
	 * 
	 * @return
	 */
	public static float getScaleFactor() {
		return drawableScaleFactor;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isTablet() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if (metrics.widthPixels >= metrics.heightPixels) {
			return (metrics.widthPixels / metrics.xdpi) > 4.5F;
		}
		else {
			return (metrics.heightPixels / metrics.ydpi) > 4.5F;
		}
	}
		
	/**
	 * 
	 * @return
	 */
	public ListAdapter getHighscoreListAdapter() {
		return mHighscoreAdapter;
	}
	

	/**
	 * 
	 * @author tay
	 *
	 */
	public class ElevatePagerAdapter extends FragmentPagerAdapter {

		/**
		 * @param fm
		 */
		public ElevatePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int position) {
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "getItem() pos="+position);
			switch (position) {
			case 0:
				return mElevate;
			case 1:
				return mHighscore;
			case 2:
				return mAbout;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	}
}
