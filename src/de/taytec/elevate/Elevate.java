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

public class Elevate extends Fragment implements OnClickListener, Elevator.ArrivedCallback {
	
	public static final int SPEED = 10;
	
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
		
		if (null != mView && mView.getParent() instanceof ViewGroup) {
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "onCreateView() - recycled");
			((ViewGroup)mView.getParent()).removeView(mView);
		}
		else {
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "onCreateView() - new");
			mView = inflater.inflate(R.layout.elevate, container, false);
		            
		    progressLevel = (ProgressBar) mView.findViewById(R.id.pbLevel);
		    progressLevel.getProgressDrawable();
		    getResources().getDrawable(R.drawable.progress_horizontal);
		    tvNewCustomers = (TextView) mView.findViewById(R.id.tvNewCustomers);
		    
		    leftElevator = (Elevator) mView.findViewById(R.id.leftElevator);
			leftElevator.setupFloors(mView);
			leftElevator.setOnClickListener(this);
		    leftElevator.setArrivedCallback(this);
		    
		    rightElevator = (Elevator) mView.findViewById(R.id.rightElevator);
			rightElevator.setupFloors(mView);
			rightElevator.setOnClickListener(this);
		    rightElevator.setArrivedCallback(this);
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey("move")) {
			if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "onCreateView() - restore state");
			gameMove = savedInstanceState.getInt("move");
			leftElevator.fromBundle(savedInstanceState.getBundle("leftElevator"));
			rightElevator.fromBundle(savedInstanceState.getBundle("rightElevator"));
		}
		else {
			startGame();
		}
		
        return mView;
    }
    
 

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "onSaveInstanceState()");
		outState.putInt("move", gameMove);
		setProgress();
		outState.putBundle("leftElevator", leftElevator.toBundle());
		outState.putBundle("rightElevator", rightElevator.toBundle());
	}

	
	

	/**
	 * behandelt Click/Touch-Events auf die Etagen
	 *
	 * @param v Angeklickter View, sollte ein 'Floor' sein
	 */
	@Override
	public void onClick(View v) {
		if (v instanceof Floor) {
			if (leftElevator.isDriving() || rightElevator.isDriving()) {
				return;
			}

			Floor f = (Floor) v;
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "onClick: "+f);

			int ls, rs;
			if (f.isLeft()) {
				ls = f.getSurfaceNumber() - 1;
				rs = Elevator.FLOORS - ls - 1;
			}
			else {
				rs = f.getSurfaceNumber() - 1;
				ls = Elevator.FLOORS - rs - 1;
			}
			
			gameMove ++;		
			genCustomer(gameMove / SPEED + 1);
			leftElevator.driveTo(ls);
			rightElevator.driveTo(rs);			
		}
	}


	/**
	 * 
	 */
	@Override
	public void onElevatorArrived(Elevator elv) {
		int maxAge = elv.timeTick();
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "maxAge="+maxAge);
		setProgress();	

		int success = leftElevator.getCountDelivered() + rightElevator.getCountDelivered();
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
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "startGame()");
		gameMove = 0;
		score = 0;
		setProgress();
		leftElevator.reset();
		rightElevator.reset();
        leftElevator.driveTo(2);
        rightElevator.driveTo(1);
        genCustomer(4);
	}


	/**
	 * 
	 */
	public void setProgress() {
		progressLevel.setProgress(gameMove);
		tvNewCustomers.setText(getResources().getString(R.string.new_customers)+(1+gameMove/SPEED));
	}
	
	
}

