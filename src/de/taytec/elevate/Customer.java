package de.taytec.elevate;

import java.io.Serializable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AndroidRuntimeException;
import android.os.*;
import android.content.res.*;
import android.util.*;

/**
 * Definition eines einzelnen Reisenden
 */
class Customer implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -86194320056748255L;
	
	/**
	 * Zieletage des Reisenden
	 */
	private int floor;
	/**
	 * Wartezeit des Reisenden in der Eingangslobby
	 */
	private int age;
	
	/**
	 * Mini Farbicon mit der Zielfarbe
	 */
	private Drawable icon;	
	
	/**
	 * Grafik-Resourcen für die Farbicons, die den Reisenden im Eingang und im Aufzug repräsentieren
	 *
	 * @todo Ersetzen der Icons durch gefüllte Rechtecke
	 */
	private static final int[] leftIds = {
				-1,
				R.drawable.lmini2,
				R.drawable.lmini3,
				R.drawable.lmini4,
				R.drawable.lmini5
			};
	
	/**
	 * Grafik-Resourcen für die Farbicons, die den Reisenden im Eingang und im Aufzug repräsentieren
	 *
	 * @todo Ersetzen der Icons durch gefüllte Rechtecke
	 */
	private static final int[] rightIds = {
				-1,
				R.drawable.rmini2,
				R.drawable.rmini3,
				R.drawable.rmini4,
				R.drawable.rmini5
	};
	
	
	/**
	 * Konstruktor
	 *
	 * @param context Applikations Kontext
	 * @param elevator Aufzug den der Reisende benutzen soll
	 */
	public Customer(Context context, Elevator elevator) {
		floor = 1 + (int)(Math.random() * (Elevator.FLOORS - 1));
		age = 0;		
		initialze(elevator, context);
	}
	
	/**
	 * Konstruktor - Der Instanzstatus wird wieder hergestellt
	 *
	 * @param context Applikations Kontext
	 * @param elevator Aufzug den der Reisende benutzen soll
	 */
	public Customer(Context context, Elevator elevator, Bundle state)
	{
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "___()");
		floor = state.getInt("floor");
		age = state.getInt("age");
		initialze(elevator, context);
	}

	
	/**
	 * Initialisierung für die intern benutzten Resourcen
	 *
	 * @param context Applikations Kontext
	 * @param elevator Aufzug den der Reisende benutzen soll
	 */
	private void initialze(Elevator elevator, Context context) throws Resources.NotFoundException
	{
		int id = elevator.isLeftSide() ? leftIds[floor] : rightIds[floor];
		icon = context.getResources().getDrawable(id);
		icon.setBounds(0, 0, ElevateActivity.getScaledWidth(icon), ElevateActivity.getScaledHeight(icon));
		
//		if (ElevateActivity.DEBUG) Log.d("Elevate::Customer", "floor: "+String.valueOf(floor));
	}
	
	/**
	 * liefert die Nummer der Zieletage
	 *
	 * @return Nummer der Zieletage
	 */
	public int getFloor() {
		return floor;
	}
	
	
	/**
	 * liefert die Anzahl der Runden, die der Reisende am Eingang wartet
	 *
	 * @return Anzahl der Warterunden
	 */
	public int getAge() {
		return age;
	}
	
	/**
	 * inkrementiert die Anzahl der Warterunden
	 *
	 * @return Anzahl der Warterunden
	 */
	public int timeTick() {
		return ++age;
	}
	
	/**
	 * Liefert die Grafik für das kleine Farbicon, das den Reisenden repäsentiert
	 *
	 * @return Gafik des Farbicons
	 */
	public Drawable getDrawable() {
		return icon;
	}
	
	/**
	 * serialisiere einen Reisenden um den Zustand nach Aktivitätsänderungen
	 * wieder herstellen zu können
	 */
	public Bundle toBundle() {
		if (ElevateActivity.DEBUG) Log.d(getClass().getSimpleName(), "toBundle()");
		Bundle out = new Bundle();
		out.putInt("floor", floor);
		out.putInt("age", age);
		return out;
	}

	/**
	 * Bescreibung des Reisenden für Debug-Zwecke
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Customer(f=" + floor + ", a=" + age + ")";
	}
}
