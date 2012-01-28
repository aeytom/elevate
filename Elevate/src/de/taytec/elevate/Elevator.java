/**
 * 
 */
package de.taytec.elevate;

import java.util.ArrayList;
import java.util.Iterator;

import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

/**
 * @author tay
 * 
 */
public class Elevator extends View {
	/**
	 * 
	 */
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private static final int CUSTOMERS_PER_ROW = 4;
	private static final int CAPACITY = 8;
	private Drawable drwElevatorEmpty;
	private Drawable drwElevator;
	private int floor;
	private Paint pCable;
	private boolean leftSide = true;
	private Floor[] floors;
	private ArrayList<Customer> travelers;
	private int success;
	private Callback callback;


	/**
	 * @param context
	 */
	public Elevator(Context context) {
		super(context);
		initialize(null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public Elevator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Elevator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	/**
	 * 
	 * @param attrs
	 */
	private void initialize(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Elevator);
			setLeftSide(a.getBoolean(R.styleable.Elevator_leftSide, true));
			a.recycle();
		}
		drwElevator = getResources().getDrawable(R.drawable.elevator);
		drwElevatorEmpty = getResources().getDrawable(R.drawable.elevatorempty);
		
		pCable = new Paint();
		pCable.setColor(Color.BLACK);
		pCable.setStrokeWidth(0);
		
		travelers = new ArrayList<Customer>();
		floor = -1;
		success = 0;
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0;
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			width = specSize;
		} else {
			width = Elevate.getScaledWidth(drwElevator) + getPaddingLeft()
					+ getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST) {
				width = Math.min(width, specSize);
			}
		}

		int height = 0;
		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			height = specSize;
		} else {
			Drawable mini = getResources().getDrawable(R.drawable.lmini2);
			height = Elevate.FLOORS * Elevate.getScaledHeight(drwElevator) + 2 * Elevate.getScaledHeight(mini) + 2;
			if (specMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, specSize);
			}
		}

//		Log.d("Elevate::Elevator",
//				"onMeasure() mode:" + specMode + " size:"
//						+ specSize + " result: "
//						+ width+","+height);
		setMeasuredDimension(width, height);
	}

	/** 
	 * delay postioning of elevator to time after measuring the view size
	 * 
	 * @see android.view.View#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (! changed) {
			driveToFloor(floor);
		}
	}

	/**
	 * draw the elevator to view
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode()) {
			Paint p = new Paint();
			p.setColor(color.darker_gray);
			canvas.drawPaint(p);
			return;
		}

		// draw elevator
		Drawable elevator = travelers.isEmpty() ? drwElevatorEmpty : drwElevator;
		int top = (Elevate.FLOORS - 1) * Elevate.getScaledHeight(elevator);
		elevator.setBounds(0, top, Elevate.getScaledWidth(elevator), top + Elevate.getScaledHeight(elevator));
		elevator.draw(canvas);

		// draw cable
		canvas.drawLine(getWidth() / 2F, 0F, getWidth() / 2F, (float) top, pCable);

		// draw customers
		Rect posElevator = elevator.getBounds();
		for (int i = travelers.size() - 1; i >= 0; i--) {
			Drawable cd = travelers.get(i).getDrawable();
			int ypos = 1 + posElevator.bottom + (1 + Elevate.getScaledHeight(cd)) * (i / CUSTOMERS_PER_ROW);
			int xpos = (i % CUSTOMERS_PER_ROW) * (1 + Elevate.getScaledWidth(cd));
			cd.setBounds(xpos, ypos, xpos + Elevate.getScaledWidth(cd),
					ypos + Elevate.getScaledHeight(cd));
			cd.draw(canvas);
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View#onAnimationEnd()
	 */
	@Override
	protected void onAnimationEnd() {
//		Log.d("Elevator", "onAnimationEnd() "+toString());
		exchangeCustomers();
		if (null != callback) {
			callback.onElevatorArived(this);
		}
		super.onAnimationEnd();
	}

	/**
	 * @param floor drive to floor to set
	 * @param generateNewCustomers generate number of new customers when elevator stops  
	 */
	public void driveToFloor(int floor) {
		int from = 0 - this.floor * Elevate.getScaledHeight(drwElevator);
		int to = 0 - floor * Elevate.getScaledHeight(drwElevator);
		if (to <= 0) {
//			Log.d("Elevator", "driveToFloor() from: "+from+" to: "+to);
			TranslateAnimation an = new TranslateAnimation(0, 0, from, to);
			an.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
					android.R.anim.accelerate_decelerate_interpolator));
			an.setDuration(Math.abs(this.floor - floor) * 200 + 100);
			an.setFillAfter(true);
			setAnimation(an);
			startAnimation(an);
		}
		this.floor = floor;
	}

	/**
	 * @param leftSide the leftSide to set
	 */
	public void setLeftSide(boolean leftSide) {
		this.leftSide = leftSide;
	}

	/**
	 * @return the leftSide
	 */
	public boolean isLeftSide() {
		return leftSide;
	}

	/**
	 * @param floors the floors to set
	 */
	public void setFloors(Floor[] floors) {
		this.floors = floors;
	}

	/**
	 * @return the floors
	 */
	public Floor[] getFloors() {
		return floors;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Elevator [floor=" + floor + ", travelers=" + travelers + "]";
	}


	/**
	 * 
	 */
	public void exchangeCustomers() {
		if (floors[floor].isEntry()) {
			// get customers from floor
			while (travelers.size() < CAPACITY) {
				Customer c = floors[floor].getWaitingCustomer();
				if (c == null) {
					break;
				}
				travelers.add(c);
			}
		}
		else {
			// put customers to floor
			floors[floor].clearFloor();
			Iterator<Customer> i = travelers.iterator();
			while (i.hasNext()) {
				if (floor == i.next().getFloor()) {
					floors[floor].addOnePeople();
					i.remove();
					success++;
				}
			}
		}
	}

	/**
	 * 
	 */
	public void genWaitingCustomer() {
		for (int i=0; i<floors.length; i++) {
			if (floors[i].isEntry()) {
				floors[i].addCustomer(new Customer(getContext(), this.isLeftSide() ? LEFT : RIGHT));
				break;
			}
		}
	}

	
	/**
	 * 
	 * @return
	 */
	public int timeTick() {
		int maxAge = -1;
		for(int i = 0; i<floors.length; i++) {
			maxAge = Math.max(floors[i].timeTick(), maxAge);
		}
		invalidate();
		return maxAge;
	}

	/**
	 * @return the driving
	 */
	public boolean isDriving() {
		Animation an = getAnimation();
		return an != null && ! an.hasEnded();
	}

	/**
	 * @return the success
	 */
	public int getSuccess() {
		return success;
	}

	/**
	 * 
	 */
	public void reset() {
		initialize(null);
		for (int i = 0; i < floors.length; i++) {
			floors[i].reset();
		}
	}
	/**
	 * @param callback the callback to set
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	
	public interface Callback {

		/**
		 * 
		 * @param elv
		 */
		public void onElevatorArived(Elevator elv);
	}

}
