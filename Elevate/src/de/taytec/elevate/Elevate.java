package de.taytec.elevate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.BoringLayout.Metrics;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Elevate extends Activity implements OnClickListener, Elevator.Callback {
	
	public static final int FLOORS = 4;
	public static final int SPEED = 10;

	private static final int DIALOG_HELP = 1;
	private static final int DIALOG_HIGHSCORE = 2;
	private static final int DIALOG_POSTSCORE = 3;
		
	private static int[] leftFloorId = {
			R.id.svLeftEntry,
			R.id.svLeftFloor2,
			R.id.svLeftFloor3,
			R.id.svLeftFloor4
	};
	
	private static int[] rightFloorId = {
			R.id.svRightEntry,
			R.id.svRightFloor2,
			R.id.svRightFloor3,
			R.id.svRightFloor4
	};
	
	private Elevator leftElevator;
	private Elevator rightElevator;
	private int gameMove = 0;
	private boolean gameOver;
	private ProgressBar progressLevel;
	private TextView tvNewCustomers;
	private Drawable drwProgressWarn;
	private Drawable drwProgessStd;
	private Highscore hiscore;
	private int score;
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
        drawableScaleFactor = metrics.density * 320F / (146F + 86F +146F);
        Log.d("Elevate", metrics.toString());
        Log.d("Elevate", "scale factor: "+drawableScaleFactor);
        
        setContentView(R.layout.main);
        
        progressLevel = (ProgressBar) findViewById(R.id.pbLevel);
        drwProgessStd = progressLevel.getProgressDrawable();
        drwProgressWarn = getResources().getDrawable(R.drawable.progress_horizontal);
        tvNewCustomers = (TextView) findViewById(R.id.tvNewCustomers);
        
    	Floor[] leftFloors = new Floor[FLOORS];
    	Floor[] rightFloors = new Floor[FLOORS];
        for (int i = 0; i < FLOORS; i++) {
        	Floor f = (Floor) findViewById(leftFloorId[i]);
        	f.setOnClickListener(this);
        	leftFloors[i] = f;
        	f = (Floor) findViewById(rightFloorId[i]);
        	f.setOnClickListener((android.view.View.OnClickListener) this);
        	rightFloors[i] = f;
        }
        
        leftElevator = (Elevator) findViewById(R.id.leftElevator);
        leftElevator.setFloors(leftFloors);
        leftElevator.setCallback(this);
        
        rightElevator = (Elevator) findViewById(R.id.rightElevator);
        rightElevator.setFloors(rightFloors);
        rightElevator.setCallback(this);
        
        hiscore = new Highscore(this);
        
        startGame();
    }
    
    
 	/**
	 * 
	 */
	@Override
	public void onClick(View v) {
		if (v instanceof Floor) {
			if (leftElevator.isDriving() || rightElevator.isDriving()) {
				return;
			}

			Floor f = (Floor) v;
//			Log.d("Elevate", "click: "+f);
			int ls;
			int rs;
			if (f.isFloorLeft()) {
				ls = f.getFloorSurface() - 1;
				rs = FLOORS - ls - 1;
			}
			else {
				rs = f.getFloorSurface() - 1;
				ls = FLOORS - rs - 1;
			}
			
			gameMove ++;		
			genCustomer(gameMove / SPEED + 1);
			leftElevator.driveToFloor(ls);
			rightElevator.driveToFloor(rs);			
		}
	}


	/**
	 * 
	 */
	@Override
	public void onElevatorArived(Elevator elv) {
		int maxAge = elv.timeTick();
//		Log.d("Elevate", "maxAge="+maxAge);
		// show game over warning
		setProgress(maxAge > Floor.MAXWAIT);	

		int success = leftElevator.getSuccess() + rightElevator.getSuccess();
		if (gameMove > 0) {
			score = Math.max(0,(111 * success / gameMove) + (gameMove * 122) - 500);
		}
		
		// check game over
		if (maxAge > (Floor.MAXWAIT + 1)) {
			gameOver = true;
			if (haveNetworking()) {
				if (hiscore.getLowestHiscore() < score)
				{
					showDialog(DIALOG_POSTSCORE);
				}
				else {
					showDialog(DIALOG_HIGHSCORE);
				}
			}
		}		
	}


	/**
	 * 
	 */
	public void genCustomer(int count) {
		for (int i = 0; i < count; i++) {
			leftElevator.genWaitingCustomer();
			rightElevator.genWaitingCustomer();
		}
	}
	
	/**
	 * reset game
	 * 
	 * @param v
	 */
	public void onClickPlay(View v)	{
		startGame();
	}
	
	/**
	 * 
	 */
	public void startGame()
	{
//		Log.d("Elevate", "startGame()");
		gameOver = false;
		gameMove = 0;
		score = 0;
		setProgress(false);
		leftElevator.reset();
		rightElevator.reset();
        leftElevator.driveToFloor(2);
        rightElevator.driveToFloor(1);
        genCustomer(4);
	}


	/**
	 * 
	 */
	public void setProgress(boolean warn) {
//		Drawable drw = progressLevel.getProgressDrawable();
//		Drawable drwWant = warn ? drwProgressWarn : drwProgessStd;
//		if (drw != drwWant) {
//			progressLevel.setProgressDrawable(drwWant);
//		}
		progressLevel.setProgress(gameMove);
		tvNewCustomers.setText(getResources().getString(R.string.new_customers)+(1+gameMove/SPEED));
	}
	
	/**
	 * 
	 * @param v
	 */
	public void onClickHighscore(View v) {
//		Log.d("Elevate", "show highscore");
		showDialog(DIALOG_HIGHSCORE);
	}
	
	/**
	 * 
	 * @param v
	 */
	public void onClickHelp(View v) {
//		Log.d("Elevate", "show help");
		showDialog(DIALOG_HELP);
	}

	
    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dlg = null;
		switch (id) {
		case DIALOG_HELP:
			dlg = createAboutDialog();
			break;
		case DIALOG_HIGHSCORE:
			dlg = createHighscoreDialog();
			break;
		case DIALOG_POSTSCORE:
			dlg = createPostScoreDialog();
			break;
		}
		return dlg;
	}

	

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (DIALOG_HIGHSCORE == id) {
			TextView tvScoreValue = (TextView) dialog.findViewById(R.id.tvScoreValue);
			tvScoreValue.setText(score > 0 ? String.valueOf(score) : "-");
			ListView lvHighscore = (ListView) dialog.findViewById(R.id.lvHighscore);
			if (! hiscore.setupScoreData()) {
				lvHighscore.setVisibility(View.INVISIBLE);
			}
			else {
				lvHighscore.setVisibility(View.VISIBLE);
			}
		}
		else if (DIALOG_POSTSCORE == id) {
			TextView tvScoreValue = (TextView) dialog.findViewById(R.id.tvScoreValue);
			tvScoreValue.setText(score > 0 ? String.valueOf(score) : "-");
		}
	}


	/**
	 * 
	 * @return
	 */
	private Dialog createHighscoreDialog() {
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.highscore_dialog);
		dialog.setTitle(getResources().getString(R.string.highscore));			
		ListView lvHighscore = (ListView) dialog.findViewById(R.id.lvHighscore);
		lvHighscore.setAdapter(hiscore);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (gameOver) {
					startGame();
				}
			}});
		return dialog;
	}


	/**
	 * 
	 * @return
	 */
	private Dialog createPostScoreDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.post_hiscore);
		dialog.setTitle(getResources().getString(R.string.highscore));
		Button btnPost = (Button) dialog.findViewById(R.id.btnPost);
		btnPost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText etNick = (EditText) dialog.findViewById(R.id.etNick);
				Editable nick = etNick.getText();
				dialog.dismiss();
				hiscore.postScore(nick, score);
				showDialog(DIALOG_HIGHSCORE);
			}});
		Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				showDialog(DIALOG_HIGHSCORE);
			}});
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showDialog(DIALOG_HIGHSCORE);
			}});
		return dialog;
	}


	/**
     * 
     * @return
     */
	protected AlertDialog createAboutDialog() {	
		String versionName = "";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		} 
		catch (NameNotFoundException e) {
			Log.e(getPackageName(), "createAboutDialog() : PackageManager.GET_META_DATA", e);
		}	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
		.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.about).replace("VERSION", versionName)))
		.setCancelable(false)
		.setPositiveButton(R.string.Button_OK,		
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
	

	/**
	 * 
	 * @return
	 */
	public boolean haveNetworking() {
		String service = Context.CONNECTIVITY_SERVICE;
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(service);
		NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
		if (null == netInfo || ! netInfo.isAvailable()) {
//			Log.d("Elevate", "Network not available");
			return false;
		}
		return true;
	}
	
	// DisplayMetrics{density=1.5, width=480, height=800, scaledDensity=1.5, xdpi=240.0, ydpi=240.0}
	// DisplayMetrics{density=0.75, width=240, height=320, scaledDensity=0.75, xdpi=120.0, ydpi=120.0}
	// DisplayMetrics{density=1.0, width=800, height=1280, scaledDensity=1.0, xdpi=165.03554, ydpi=165.20325}

	public static int getScaledWidth(Drawable drawable) {
		return (int) (drawableScaleFactor * drawable.getIntrinsicWidth());
	}
	
	public static int getScaledHeight(Drawable drawable) {
		return (int) (drawableScaleFactor * drawable.getIntrinsicHeight());
	}
}

