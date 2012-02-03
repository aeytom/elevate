/**
 * 
 */
package de.taytec.elevate;

import java.util.ArrayList;
import java.util.ListIterator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author tay
 * 
 */
public class Floor extends View {

	public static final int MAXCALM = 2;
	public static final int MAXWAIT = 3;
	
	private ArrayList<Runnable> addActions = new ArrayList<Runnable>();
	protected int animationCounter;
	private Runnable animator = null;
	private AnimationDrawable drwUpturn;
	private AnimationDrawable drwWait;
	private AnimationDrawable drwWorker;
	private boolean entry = false;
	private boolean floorLeft = true;
	private int floorSurface = 1;
	private Drawable labelGraphic;
	private int peopleCount = 0;
	private Rect posWorker;
	private ArrayList<Customer> waiting;
	private int age;

	/**
	 * @param context
	 */
	public Floor(Context context) {
		super(context);
		initialize(null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public Floor(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Floor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	/**
	 * 
	 * @param customer
	 */
	public void addCustomer(Customer customer) {
		if (waiting != null) {
			waiting.add(customer);
		}
	}

	/**
	 * 
	 */
	public void addOnePeople() {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				setPeopleCount(getPeopleCount() + 1);
				invalidate();				
			}
		};
		addActions.add(action);
		postDelayed(action, addActions.size() * 400);
	}

	/**
	 * 
	 */
	public void clearFloor() {
		setPeopleCount(0);
		for (int i = 0; i < addActions.size(); i++) {
			removeCallbacks(addActions.get(i));
		}
		addActions.clear();
		age = 0;
		invalidate();
	}

	/**
	 * @return the floorSurface
	 */
	public int getFloorSurface() {
		return floorSurface;
	}
	
	/**
	 * @return the peopleCount
	 */
	public int getPeopleCount() {
		return peopleCount;
	}
	
	/**
	 * get one waiting customer from waiting queue
	 * 
	 * @return customer or null on empty queue
	 */
	public Customer getWaitingCustomer() {
		Customer customer = null;
		if (waiting.size() > 0) {
			customer  = waiting.get(0);
			waiting.remove(0);
		}
		return customer;
	}

	/**
	 * 
	 * @param context 
	 * @param attrs
	 */
	protected void initialize(AttributeSet attrs) {		
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Floor);
			setEntry(a.getBoolean(R.styleable.Floor_entry, false));
			setFloorLeft(a.getBoolean(R.styleable.Floor_floorLeft, true));
			setFloorSurface(a.getInt(R.styleable.Floor_surface, 4));
			labelGraphic = getResources().getDrawable(a.getResourceId(R.styleable.Floor_src, R.drawable.lfloorentry));
//			if (ElevateActivity.DEBUG) Log.d("labelGraphic", "w="+labelGraphic.getIntrinsicWidth()+" h="+labelGraphic.getIntrinsicHeight());
			labelGraphic.setBounds(0, 0, ElevateActivity.getScaledWidth(labelGraphic), ElevateActivity.getScaledHeight(labelGraphic));
			a.recycle();
		}
		setClickable(true);
		clearFloor();
		
		if (isEntry()) {
			waiting = new ArrayList<Customer>();
		}
		
		if (isEntry()) {
			drwWorker = (AnimationDrawable) getResources().getDrawable(isFloorLeft() ? R.drawable.rwalk : R.drawable.lwalk);
			drwWait = (AnimationDrawable) getResources().getDrawable(R.drawable.wait);
			drwWait.setBounds(
					0, ElevateActivity.getScaledHeight(labelGraphic), 
					ElevateActivity.getScaledWidth(drwWait), ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwWait));
			drwUpturn = (AnimationDrawable) getResources().getDrawable(R.drawable.upturn);
			drwUpturn.setBounds(
					0, ElevateActivity.getScaledHeight(labelGraphic), 
					ElevateActivity.getScaledWidth(drwUpturn), ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwUpturn));			
		}
		else {
			drwWorker = (AnimationDrawable) getResources().getDrawable(isFloorLeft() ? R.drawable.lwalk : R.drawable.rwalk);
		}
		drwWorker.setBounds(
				0, ElevateActivity.getScaledHeight(labelGraphic), 
				ElevateActivity.getScaledWidth(drwWorker), ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwWorker));
		posWorker = drwWorker.copyBounds();
		
		if (null == animator) {
			animator = new Runnable() {
				public void run() {
					animationCounter ++;
					invalidate();
					postDelayed(this, 100);
				}
			};
			postDelayed(animator, 100);
		}
	}

	/**
	 * @return the entry
	 */
	public boolean isEntry() {
		return entry;
	}

	/**
	 * @return the floorLeft
	 */
	public boolean isFloorLeft() {
		return floorLeft;
	}

	/**
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();

		if (isInEditMode()) {
			Paint p = new Paint();
			p.setColor(Color.GRAY);
			canvas.drawPaint(p);
			return;
		}
		
		labelGraphic.draw(canvas);

		Drawable walker;
		drwWorker.copyBounds(posWorker);
		int offset;
		if (isFloorLeft()) {
			posWorker.offsetTo(width - ElevateActivity.getScaledWidth(drwWorker), posWorker.top);
			offset = 0 - ElevateActivity.getScaledWidth(drwWorker);
		} else {
			offset = ElevateActivity.getScaledWidth(drwWorker);
		}
		if (isEntry()) {
			ListIterator<Customer> i = waiting.listIterator();
			while (i.hasNext()) {
				Customer customer = i.next();
				int age = customer.getAge();
				if (age > MAXWAIT) {
					walker = drwUpturn.getFrame(animationCounter % drwUpturn.getNumberOfFrames());
				}
				else if (age > MAXCALM) {
					walker = drwWait.getFrame(animationCounter % drwWait.getNumberOfFrames());
				}
				else {
					walker = drwWorker.getFrame(animationCounter % drwWorker.getNumberOfFrames());

				}
				posWorker.right = posWorker.left + ElevateActivity.getScaledWidth(walker);
				walker.setBounds(posWorker);
				walker.setAlpha(255);
				walker.draw(canvas);
				
				Drawable mini = customer.getDrawable();
				int miniLeft = posWorker.left + (ElevateActivity.getScaledWidth(walker) - ElevateActivity.getScaledWidth(mini)) / 2;
				mini.setBounds(miniLeft, posWorker.bottom + 1, 
						miniLeft + ElevateActivity.getScaledWidth(mini), posWorker.bottom + 1 + ElevateActivity.getScaledHeight(mini));
				mini.draw(canvas);
				
				posWorker.offset(offset, 0);
			}
		}
		else {
			walker = drwWorker.getFrame(animationCounter % drwWorker.getNumberOfFrames());
			for (int i = getPeopleCount(); i > 0; i--) {
				walker.setBounds(posWorker);
				walker.setAlpha(Math.max(0,255 - 85 * Math.max(0,age-1)));
				walker.draw(canvas);
				posWorker.offset(offset, 0);
			}
		}
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
			width = ElevateActivity.getScaledWidth(labelGraphic)
					+ getPaddingLeft()
					+ getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST) {
				width = Math.min(width, specSize);
			}
		}

//		if (ElevateActivity.DEBUG) Log.d("Elevate:Floor", "onMeasure width ("+specMode+") "+specSize+" => "+width);
		int height = 0;
		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			height = specSize;
		} else {
			Drawable mini = getResources().getDrawable(R.drawable.lmini2);
			height = ElevateActivity.getScaledHeight(labelGraphic)
					+ ElevateActivity.getScaledHeight(drwWorker) 
					+ (isEntry() ? ElevateActivity.getScaledHeight(mini)+1 : 0)
					+ getPaddingTop()
					+ getPaddingBottom();
			if (specMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, specSize);
			}
		}

//		if (ElevateActivity.DEBUG) Log.d("Elevate:Floor", "onMeasure height ("+specMode+") "+specSize+" => "+height);
		setMeasuredDimension(width, height);
	}


	/**
	 * @param entry the entry to set
	 */
	public void setEntry(boolean entry) {
		this.entry = entry;
	}

	/**
	 * @param floorLeft
	 *            the floorLeft to set
	 */
	public void setFloorLeft(boolean floorLeft) {
		this.floorLeft = floorLeft;
	}

	/**
	 * @param floorSurface
	 *            the floorSurface to set
	 */
	public void setFloorSurface(int floorSurface) {
		this.floorSurface = floorSurface;
	}

	/**
	 * @param peopleCount
	 *            the peopleCount to set
	 */
	public void setPeopleCount(int peopleCount) {
		this.peopleCount = peopleCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isEntry()) {
			return "Floor [floorLeft=" + floorLeft + ", floorSurface=" + floorSurface+ ", wating=" + waiting + "]";
		}
		else {
			return "Floor [floorLeft=" + floorLeft + ", peopleCount=" + peopleCount	+ ", floorSurface=" + floorSurface + ", age=" + age + "]";
		}
	}

	/**
	 * @return 
	 * 
	 */
	public int timeTick() {
		int maxAge = -1;
		if (isEntry()) {
			ListIterator<Customer> i = waiting.listIterator();
			while (i.hasNext()) {
				maxAge = Math.max(i.next().timeTick(), maxAge);
			}
		}
		else {
			age ++;
		}
//		if (ElevateActivity.DEBUG) Log.d("Elvate:Floor", toString());
		return maxAge;
	}

	/**
	 * 
	 */
	public void reset() {
		initialize(null);
	}

	
}
