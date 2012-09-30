/**
 * 
 */
package de.taytec.elevate;

import android.R.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import java.util.*;
import android.app.*;

/**
 * @author tay
 * 
 */
public class Elevator extends View
{
	
	/**
	 * Anzahl der Etagen
	 */
	public static final int FLOORS = 4;
	/**
	 * Anzahl der Marker der Reisenden in einer Zeile im Aufzug
	 */
	private static final int CUSTOMERS_PER_ROW = 4;
	/** 
	 * Anzahl der Reisenden die ein Aufzug fasst
	 */
	private static final int CAPACITY = 8;
	/**
	 * Liste der View-Ids der Etagen für die linke Seite
	 */
	private static final int[] leftFloorId = {
		R.id.svLeftEntry,
		R.id.svLeftFloor2,
		R.id.svLeftFloor3,
		R.id.svLeftFloor4
	};
	/**
	 * Liste der View-Ids der Etagen für die rechte Seite
	 */	
	private static final int[] rightFloorId = {
		R.id.svRightEntry,
		R.id.svRightFloor2,
		R.id.svRightFloor3,
		R.id.svRightFloor4
	};
	
	/**
	 * Grafik für einen leeren Aufzug
	 */
	private Drawable drwElevatorEmpty;
	/**
	 * Grafik für einen besetzten Aufzug
	 */
	private Drawable drwElevator;
	/**
	 * grafische Defintion des Aufzugkabels
	 */
	private Paint pCable;
	/**
	 * Flag ob es sixh um den linken Aufzug handelt
	 */
	private boolean isLeft = true;
	/*
	 * zu benachrichtigende Instanz bei Ankunft des Aufzugs in einer Etage
	 */
	private ArrivedCallback arrivedCallback;
	/**
	 * Instanz der Lobby
	 */
	private Floor entryFloor;
	
	/*
	 * Status des Aufzugs
	 */
	 
	/**
	 * Zugehörige Etagen
	 */
	private Floor[] floors;
	/**
	 * Im Aufzug befindliche Reisende
	 */
	private ArrayList<Customer> travelers;
	/**
	 * Position des Aufzugs
	 */
	private int position;
	/**
	 * Anzahl erfolgreich tranportierter Reisender
	 */
	private int countDeliveredCustomers;

	
	/**
	 * Konstruktor
	 *
	 * @param context
	 * @param attrs
	 */
	public Elevator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "__()");

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Elevator);
		isLeft = a.getBoolean(R.styleable.Elevator_leftSide, true);
		a.recycle();
		
		drwElevator = getResources().getDrawable(R.drawable.elevator);
		drwElevatorEmpty = getResources().getDrawable(R.drawable.elevatorempty);
		
		pCable = new Paint();
		pCable.setColor(Color.BLACK);
		pCable.setStrokeWidth(0);
	}

	/**
	 * zugehörige Etagen zuweisen - Einmaliger Aufruf aus Elevate-Klasse
	 * während der Intitialisierung 
	 *
	 * @param view Parent View über den sich die Etagen Views finden lassen
	 */
	public void setupFloors(View view)
	{
		floors = new Floor[FLOORS];
		for (int i = 0; i < FLOORS; i++)
		{
			Floor f = (Floor) view.findViewById(isLeft ? leftFloorId[i] : rightFloorId[i]);
			floors[i] = f;
			if (f.isEntry()) {
				entryFloor = f;
			}
		}
		
		reset();
	}
	
	
	/**
	 * Reset Aufzugstatus bei Spielanfang
	 */
	public void reset()
	{
		travelers = new ArrayList<Customer>();
		position = -1;
		countDeliveredCustomers = 0;
		
		for (int i = 0; i < floors.length; i++)
		{
			floors[i].reset();
		}
	}
	
	
	/**
	 * Status des Aufzugs und der angeschlossenen Etagen
	 * sichern.
	 *
	 * @return gesicherter Zustand des Aufzugs und der verbundenen Etagen 
	 */
	protected Bundle toBundle() {
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "toBundle()");
		Bundle out = new Bundle();
		out.putInt("floor", position);
		out.putInt("success", countDeliveredCustomers);
		
		Bundle[] sp = new Bundle[floors.length];
		for (int i = floors.length -1 ; i >= 0; i--) {
			sp[i] = floors[i].toBundle();
		}
		out.putParcelableArray("floors", sp);

		Bundle[] tl = new Bundle[travelers.size()];
		for (int i = tl.length - 1; i >= 0; i--) {
			tl[i] = travelers.get(i).toBundle();
		}
		out.putParcelableArray("travelers", tl);
		
		return out;
	}
	
	
	/**
	 * Zustand des Aufzugs bei Wiederaktivierung wieder herstellen
	 *
	 * @param state Gesicherter alter Zustand 
	 */
	protected void fromBundle(Bundle state) {
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "fromBundle()");
		setPosition(state.getInt("floor"));
		countDeliveredCustomers = state.getInt("success");
		Bundle[] sp = (Bundle[]) state.getParcelableArray("floors");
		for (int i = floors.length - 1; i >= 0; i--) {
			floors[i].fromBundle(this, sp[i]);
		}

		Bundle[] tl = (Bundle[]) state.getParcelableArray("travelers");
		travelers.clear();
		for (int i = 0; i < tl.length; i++) {
			Customer c = new Customer(getContext(), this, tl[i]);
			travelers.add(c);
		}
	}
	
	/**
	 * aktuelle Etage des Aufzugs einstellen
	 */
	private void setPosition(int position)
	{
		this.position = position;
		setY(0 - position * ElevateActivity.getScaledHeight(drwElevator));
	}
	
	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = 0;
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);
		
		if (specMode == MeasureSpec.EXACTLY)
		{
			width = specSize;
		}
		else
		{
			width = ElevateActivity.getScaledWidth(drwElevator) + getPaddingLeft()
				+ getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST)
			{
				width = Math.min(width, specSize);
			}
		}
		
		int height = 0;
		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);
		
		if (specMode == MeasureSpec.EXACTLY)
		{
			height = specSize;
		}
		else
		{
			Drawable mini = getResources().getDrawable(R.drawable.lmini2);
			height = Elevator.FLOORS * ElevateActivity.getScaledHeight(drwElevator) + 2 * ElevateActivity.getScaledHeight(mini) + 2;
			if (specMode == MeasureSpec.AT_MOST)
			{
				height = Math.min(height, specSize);
			}
		}
		
//		if (ElevateActivity.DEBUG) Log.d("Elevate::Elevator",
//				"onMeasure() mode:" + specMode + " size:"
//						+ specSize + " result: "
//						+ width+","+height);
		setMeasuredDimension(width, height);
	}
	
	
	/**
	 * draw the elevator to view
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (isInEditMode())
		{
			Paint p = new Paint();
			p.setColor(color.darker_gray);
			canvas.drawPaint(p);
			return;
		}
		
		// draw elevator
		Drawable elevator = travelers.isEmpty() ? drwElevatorEmpty : drwElevator;
		int top = (Elevator.FLOORS - 1) * ElevateActivity.getScaledHeight(elevator);
		elevator.setBounds(0, top, ElevateActivity.getScaledWidth(elevator), top + ElevateActivity.getScaledHeight(elevator));
		elevator.draw(canvas);
		
		// draw cable
		canvas.drawLine(getWidth() / 2F, 0F, getWidth() / 2F, (float) top, pCable);
		
		// draw customers
		Rect posElevator = elevator.getBounds();
		for (int i = travelers.size() - 1; i >= 0; i--)
		{
			Drawable cd = travelers.get(i).getDrawable();
			int ypos = 1 + posElevator.bottom + (1 + ElevateActivity.getScaledHeight(cd)) * (i / CUSTOMERS_PER_ROW);
			int xpos = (i % CUSTOMERS_PER_ROW) * (1 + ElevateActivity.getScaledWidth(cd));
			cd.setBounds(xpos, ypos, xpos + ElevateActivity.getScaledWidth(cd),
						 ypos + ElevateActivity.getScaledHeight(cd));
			cd.draw(canvas);
		}
	}
	
	/**
	 * Event Handler am Ende der Anmtion des Aufzugs ander Zieletage
	 *
	 * Aufgerufen wird eine Callback-Funktion im Spiel, die dafür sorgt,dass die Reisenden
	 * entladen werden.
	 *
	 * @see android.view.View#onAnimationEnd()
	 */
	@Override
	protected void onAnimationEnd()
	{
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "onAnimationEnd() " + this);
		exchangeCustomers();
		if (null != arrivedCallback)
		{
			arrivedCallback.onElevatorArrived(this);
		}
		super.onAnimationEnd();
	}
	
	/**
	 * Aufzug animiert zu einer neuen Zieletage bewegen
	 *
	 * @param destination anzufahrende Zieletage
	 */
	public void driveTo(int destination)
	{
		int from = 0 - this.position * ElevateActivity.getScaledHeight(drwElevator);
		int to = 0 - destination * ElevateActivity.getScaledHeight(drwElevator);
		if (to <= 0)
		{
			if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "driveToFloor() from: " + from + " to: " + to);
			TranslateAnimation an = new TranslateAnimation(0, 0, from, to);
			an.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
															   android.R.anim.accelerate_decelerate_interpolator));
			an.setDuration(Math.abs(this.position - destination) * 200 + 100);
			an.setFillAfter(true);
			setAnimation(an);
			setY(0);
			startAnimation(an);
		}
		this.position = destination;
	}
	
	
	/**
	 * zeigt, ob es sich um den linken Aufzug handelt
	 *
	 * @return the leftSide
	 */
	public boolean isLeftSide()
	{
		return isLeft;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Elevator [floor=" + position + ", travelers=" + travelers + "]";
	}
	
	
	/**
	 * Reisende in der Lobby aufnehmen oder in der Zieletage ausladen
	 */
	public void exchangeCustomers()
	{
		if (floors[position].isEntry())
		{
			// get customers from floor
			while (travelers.size() < CAPACITY)
			{
				Customer c = floors[position].getWaitingCustomer();
				if (c == null)
				{
					break;
				}
				travelers.add(c);
			}
		}
		else
		{
			// put customers to floor
			floors[position].clearFloor();
			Iterator<Customer> i = travelers.iterator();
			while (i.hasNext())
			{
				if (position == i.next().getFloor())
				{
					floors[position].addOnePeople();
					i.remove();
					countDeliveredCustomers++;
				}
			}
		}
	}
	
	/**
	 * einen neuen wartenden Reisenden generieren
	 */
	public void genWaitingCustomer()
	{
		entryFloor.addCustomer(new Customer(getContext(), this));
	}
	
	
	/**
	 * eine Spielrunde weiter ziehen
	 *
	 * @return
	 */
	public int timeTick()
	{
		int maxAge = -1;
		for (int i = 0; i < floors.length; i++)
		{
			maxAge = Math.max(floors[i].timeTick(), maxAge);
		}
		invalidate();
		return maxAge;
	}
	
	/**
	 * zeigt ob sicher Aufzug in Bewegung befindet
	 *
	 * @return true, wenn in Bewegung (Animation läuft)
	 */
	public boolean isDriving()
	{
		Animation an = getAnimation();
		return an != null && ! an.hasEnded();
	}
	
	/**
	 * liefert die Anzahl der erfolgreich abgelieferten Reisenden
	 *
	 * @return Anzahl der erfolgreich abgelieferten Reisenden
	 */
	public int getCountDelivered()
	{
		return countDeliveredCustomers;
	}
	
	/**
	 * Click-Listener für alle Etagen registrieren
	 */
	public void setOnClickListener(View.OnClickListener l)
	{
		for (int i = floors.length - 1; i >= 0; i--)
		{
			floors[i].setOnClickListener(l);
		}
	}
	
	/**
	 *
	 * @param callback the callback to set
	 */
	public void setArrivedCallback(ArrivedCallback callback)
	{
		this.arrivedCallback = callback;
	}
	
	/**
	 * Interface um sich über ankommende Aufzüge Informieren zu lassen
	 */
	public interface ArrivedCallback
	{
		
		/**
		 * Handler, der über ankommende Aufzüge informiert
		 * 
		 * @param elv angekommener Aufzug
		 */
		public void onElevatorArrived(Elevator elv);
	}
	
}
