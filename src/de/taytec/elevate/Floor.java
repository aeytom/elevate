/**
 * 
 */
package de.taytec.elevate;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import java.util.*;

/**
 * @author tay
 * 
 */
public class Floor extends View {

	/**
	 * Anzahl der Wartezyklen die eine Reisender ruhig abwartet
	 */
	public static final int MAXCALM = 2;
	/**
	 * Anzahl der Wartezyklen die ein Reisender maximal wartet
	 */
	public static final int MAXWAIT = 3;
	
	/*
	 * --------------------------------------------------------------------------------
	 * Member Varablen, deren Status bei App-Lifezykluswechseln gesichert werden müssen
	 * --------------------------------------------------------------------------------
	 */
	
	/**
	 * Queue der wartenden Reisenden 
	 */
	private ArrayList<Customer> customersWaitingQueue;
	/**
	 * Taskliste für die Reisenden die gerade den Aufzug verlassen haben
	 */
	private ArrayList<Runnable> customersArrivedQueue = new ArrayList<Runnable>();
	/**
	 * Anzahl der angekommenen Reisenden in der Etage, wenn es sich nicht um 
	 * einen Eingang handelt
	 */
	private int numberCustomersArrived = 0;
	/**
	 * Zeit/Züge die seit der letzten Ankunft von Reisenden
	 * vergangen ist
	 */
	private int elapsedTimeSinceArrival;
	
	
	/*
	 * --------------------------------------------------------------------------------
	 * Member-Variablen die unabhängig von den Spielzyklen sind und die daher bei
	 * einem App-Lifezykluswechsel nicht gesichert und wieder hergestellt werden müssen.
	 * --------------------------------------------------------------------------------
	 */
	
	/**
	 * Zustandszähler aller Animationen
	 */
	protected int animationFrame;
	/**
	 * Task um die Animationen der Reisenden anzuzeigen
	 */
	private Runnable animator = null;
	/**
	 * Grafik für ungeduldigen Reisenden (nur Eingangsflur)
	 */
	private AnimationDrawable drwUpturn;
	/**
	 * Grafik für ruhig wartenden Reisenden (nur Eingangsflur)
	 */
	private AnimationDrawable drwWait;
	/**
	 * Grafik für laufenden Reisenden (nur Ausgangsflur)
	 */
	private AnimationDrawable drwWorker;
	/**
	 * Marker, ob es sich um einen Eingangsflur handelt
	 */
	private boolean isEntry = false;
	/**
	 * Marker, ob es sich um einen linken Flur handelt, wichtig für die Laufrichtung 
	 * der ankommenden Reisenden
	 */
	private boolean isLeft = true;
	/**
	 * Etage Nummer
	 */
	private int surfaceNumber = 1;
	/**
	 * Grafik für die etagenbeschriftung
	 */
	private Drawable labelGraphic;
	/**
	 * Position eines Reisenden für die Darstellung
	 */
	private Rect posWorker;
	
	
	/**
	 * Konstruktor
	 * 
	 * @param context
	 */
	public Floor(Context context) {
		super(context);
		initialize(null);
	}

	/**
	 * Konstruktor
	 * 
	 * @param context
	 * @param attrs
	 */
	public Floor(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}

	/**
	 * Konstruktor
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Floor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	/**
	 * Einen neuen wartenden Reisenden dem Flur hinzufügen
	 * 
	 * @param customer einen neuen wartenden Reisenden für den Eingang
	 */
	public void addCustomer(Customer customer) {
		if (customersWaitingQueue != null) {
			customersWaitingQueue.add(customer);
		}
	}

	/**
	 * einen ankommenden Reisenden hinzufügen/ausladen
	 * 
	 * Die Animation für das Erscheinen des Reisenden wird mit einer mit der Anzahl 
	 * bereits angegekommenen Reisenden wachsenden Verzögerung gestartet.
	 */
	public void addOnePeople() {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				setPeopleCount(getNumberOfCustomersArrived() + 1);
				invalidate();				
			}
		};
		customersArrivedQueue.add(action);
		postDelayed(action, customersArrivedQueue.size() * 400);
	}

	/**
	 * Löscht alle angekommenen Reisenden vom Flur
	 * 
	 * Alle Animationen werden beendet
	 */
	public void clearFloor() {
		setPeopleCount(0);
		for (int i = 0; i < customersArrivedQueue.size(); i++) {
			removeCallbacks(customersArrivedQueue.get(i));
		}
		customersArrivedQueue.clear();
		elapsedTimeSinceArrival = 0;
		invalidate();
	}

	/**
	 * Liefert die Etagen-Nummer
	 * 
	 * @return Etagen Nummer 
	 */
	public int getSurfaceNumber() {
		return surfaceNumber;
	}
	
	/**
	 * Liefert die Anzahl der Reisenden, die auf der Etage aktuell eingetroffen sind
	 * 
	 * @return Anzahl der angekommenen Reisenden
	 */
	public int getNumberOfCustomersArrived() {
		return numberCustomersArrived;
	}
	
	/**
	 * Gibt einen Kunden aus der Warteschlange zurück und verkürzt die
	 * Warteschlange 
	 * 
	 * @return ein wartender Reisender oder null
	 */
	public Customer getWaitingCustomer() {
		Customer customer = null;
		if (customersWaitingQueue != null && customersWaitingQueue.size() > 0) {
			customer  = customersWaitingQueue.get(0);
			customersWaitingQueue.remove(0);
		}
		return customer;
	}

	/**
	 * Allgemeine Initialisierung der Instanz
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
			// Queue der wartenden Reisenden initialisieren
			customersWaitingQueue = new ArrayList<Customer>();
			// Grafik der ruhig wartenden Reisenden initialisieren
			drwWait = (AnimationDrawable) getResources().getDrawable(R.drawable.wait);
			drwWait.setBounds(
					0, 
					ElevateActivity.getScaledHeight(labelGraphic), 
					ElevateActivity.getScaledWidth(drwWait), 
					ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwWait));
			// Grafik der wütenden wartenden Reisenden initialieren
			drwUpturn = (AnimationDrawable) getResources().getDrawable(R.drawable.upturn);
			drwUpturn.setBounds(
					0, 
					ElevateActivity.getScaledHeight(labelGraphic), 
					ElevateActivity.getScaledWidth(drwUpturn), 
					ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwUpturn));
			// Grafik der gerade angekommenden Reisenden initilisieren			
			drwWorker = (AnimationDrawable) getResources().getDrawable(
				isLeft() 
					? R.drawable.rwalk 
					: R.drawable.lwalk);
		}
		else {
			// Grafik der Reisenden, die erfolgreich ihr Ziel erreicht haben  
			drwWorker = (AnimationDrawable) getResources().getDrawable(
				isLeft() 
					? R.drawable.lwalk 
					: R.drawable.rwalk);
		}

		drwWorker.setBounds(
			0, 
			ElevateActivity.getScaledHeight(labelGraphic), 
			ElevateActivity.getScaledWidth(drwWorker), 
			ElevateActivity.getScaledHeight(labelGraphic) + ElevateActivity.getScaledHeight(drwWorker));
		posWorker = drwWorker.copyBounds();
		
		if (null == animator) {
			animator = new Runnable() {
				public void run() {
					animationFrame ++;
					invalidate();
					postDelayed(this, 100);
				}
			};
			postDelayed(animator, 100);
		}
	}

	/**
	 * Bestimmt ob der Flur ein Eingang ist
	 * 
	 * @return true, wenn der Flur ein Eingang ist
	 */
	public boolean isEntry() {
		return isEntry;
	}

	/**
	 * Bestimmt ob es sich um einen linken Flur handelt
	 * 
	 * Das ist wichtig für die Laufrichtung der Reisenden
	 * 
	 * @return true, wenn es sich um einen linken Flur handelt
	 */
	public boolean isLeft() {
		return isLeft;
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
		if (isLeft()) {
			posWorker.offsetTo(width - ElevateActivity.getScaledWidth(drwWorker), posWorker.top);
			offset = 0 - ElevateActivity.getScaledWidth(drwWorker);
		} else {
			offset = ElevateActivity.getScaledWidth(drwWorker);
		}
		if (isEntry()) {
			ListIterator<Customer> i = customersWaitingQueue.listIterator();
			while (i.hasNext()) {
				Customer customer = i.next();
				int age = customer.getAge();
				if (age > MAXWAIT) {
					walker = drwUpturn.getFrame(animationFrame % drwUpturn.getNumberOfFrames());
				}
				else if (age > MAXCALM) {
					walker = drwWait.getFrame(animationFrame % drwWait.getNumberOfFrames());
				}
				else {
					walker = drwWorker.getFrame(animationFrame % drwWorker.getNumberOfFrames());

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
			walker = drwWorker.getFrame(animationFrame % drwWorker.getNumberOfFrames());
			for (int i = getNumberOfCustomersArrived(); i > 0; i--) {
				walker.setBounds(posWorker);
				walker.setAlpha(Math.max(0,255 - 85 * Math.max(0,elapsedTimeSinceArrival-1)));
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
	 * Legt fest ob der Flur als Eingang oder Ausgang arbeitet
	 * 
	 * @param entry true, wenn es sich um einen Eingang handeln soll
	 */
	public void setEntry(boolean entry) {
		this.isEntry = entry;
	}

	/**
	 * Legt fest ob sich der Flur auf der linken oder der rechten
	 * Seite befindet.
	 * 
	 * @param floorLeft true, wenn es sich um einen linken Flur handelt
	 */
	public void setFloorLeft(boolean floorLeft) {
		this.isLeft = floorLeft;
	}

	/**
	 * @param floorSurface
	 *            the floorSurface to set
	 */
	public void setFloorSurface(int floorSurface) {
		this.surfaceNumber = floorSurface;
	}

	/**
	 * @param peopleCount
	 *            the peopleCount to set
	 */
	public void setPeopleCount(int peopleCount) {
		this.numberCustomersArrived = peopleCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isEntry()) {
			return "Floor [left=" + isLeft + ", floorSurface=" + surfaceNumber + ", waiting=" + customersWaitingQueue + "]";
		}
		else {
			return "Floor [left=" + isLeft	+ ", floorSurface=" + surfaceNumber + ", arrivied=" + numberCustomersArrived + ", age=" + elapsedTimeSinceArrival + "]";
		}
	}

	/**
	 * Eine Zeiteinheit weiterzählen 
	 * 
	 * Ist der Flur ein Eingang wird die Wartezeit des am längsten Wartenden
	 * zurückgegeben. In allen anderen Fällen ist das Ergebnis -1
	 * 
	 * @return Wartezeit des ältesten Wartenden
	 */
	public int timeTick() {
		int maxAge = -1;
		if (isEntry()) {
			ListIterator<Customer> i = customersWaitingQueue.listIterator();
			while (i.hasNext()) {
				maxAge = Math.max(i.next().timeTick(), maxAge);
			}
		}
		else {
			elapsedTimeSinceArrival ++;
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

	/**
	 *
	 */
	protected Bundle toBundle() {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "toBundle()");
		Bundle out = new Bundle();
		if (isEntry()) {
			Bundle[] cl = new Bundle[customersWaitingQueue.size()];
			for (int i = customersWaitingQueue.size() - 1; i>=0; i--) {
				cl[i] = customersWaitingQueue.get(i).toBundle();
			}
			out.putParcelableArray("waiting", cl);
		}
		else {
			out.putInt("arrived", numberCustomersArrived);
			out.putInt("elapsed", elapsedTimeSinceArrival);		
		}
		return out;
	}

	/**
	 *
	 */
	public void fromBundle(Elevator elevator, Bundle state) {
		if (ElevateActivity.DEBUG) Log.v(getClass().getSimpleName(), "fromBundle()");
		clearFloor();
		if (state.containsKey("arrived")) {
			setPeopleCount(state.getInt("arrived"));
			elapsedTimeSinceArrival = state.getInt("elapsed");
		}
		else {
			Bundle[] cl = (Bundle[]) state.getParcelableArray("waiting");
			for (int i = 0; i < cl.length; i++) {
				Customer c = new Customer(getContext(), elevator, cl[i]);
				customersWaitingQueue.add(c);
			}
			
		}
	}
	
}
