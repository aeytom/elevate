package de.taytec.elevate;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Elevate extends Fragment implements OnClickListener, Elevator.Callback {
	
	public static final int FLOORS = 4;
	public static final int SPEED = 10;

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
	private ProgressBar progressLevel;
	private TextView tvNewCustomers;
	private int score;
	private View mView;
	
	
    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d(getClass().getSimpleName(), "onCreateView()");
		
		if (null != mView && mView.getParent() instanceof ViewGroup) {
			((ViewGroup)mView.getParent()).removeView(mView);
		}
		else {
			mView = inflater.inflate(R.layout.elevate, container, false);
		//		view.setScaleX(ElevateActivity.getScaleFactor());
		//		view.setScaleY(ElevateActivity.getScaleFactor());
		            
		    progressLevel = (ProgressBar) mView.findViewById(R.id.pbLevel);
		    progressLevel.getProgressDrawable();
		    getResources().getDrawable(R.drawable.progress_horizontal);
		    tvNewCustomers = (TextView) mView.findViewById(R.id.tvNewCustomers);
		    
			Floor[] leftFloors = new Floor[FLOORS];
			Floor[] rightFloors = new Floor[FLOORS];
		    for (int i = 0; i < FLOORS; i++) {
		    	Floor f = (Floor) mView.findViewById(leftFloorId[i]);
		    	f.setOnClickListener(this);
		    	leftFloors[i] = f;
		    	f = (Floor) mView.findViewById(rightFloorId[i]);
		    	f.setOnClickListener((android.view.View.OnClickListener) this);
		    	rightFloors[i] = f;
		    }
		    
		    leftElevator = (Elevator) mView.findViewById(R.id.leftElevator);
		    leftElevator.setFloors(leftFloors);
		    leftElevator.setCallback(this);
		    
		    rightElevator = (Elevator) mView.findViewById(R.id.rightElevator);
		    rightElevator.setFloors(rightFloors);
		    rightElevator.setCallback(this);
		    
		    startGame();
		}
        return mView;
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

			Log.d(getClass().getSimpleName(), "onclick()");
			
			Floor f = (Floor) v;
			Log.d(getClass().getSimpleName(), "click: "+f);
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
		Log.d(getClass().getSimpleName(), "maxAge="+maxAge);
		// show game over warning
		setProgress(maxAge > Floor.MAXWAIT);	

		int success = leftElevator.getSuccess() + rightElevator.getSuccess();
		if (gameMove > 0) {
			score = Math.max(0,(111 * success / gameMove) + (gameMove * 122) - 500);
		}
		
		// check game over
		if (maxAge > (Floor.MAXWAIT + 1)) {
			((ElevateActivity)getActivity()).showHighscore(score);
			startGame();
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
		Log.d(getClass().getSimpleName(), "starGame()");
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
		progressLevel.setProgress(gameMove);
		tvNewCustomers.setText(getResources().getString(R.string.new_customers)+(1+gameMove/SPEED));
	}
	
	
}

