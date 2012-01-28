package de.taytec.elevate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AndroidRuntimeException;

class Customer {
	
	private int floor;
	private int age;
	private int type;
	private Drawable icon;
	
	private static final int[] leftIds = {
				-1,
				R.drawable.lmini2,
				R.drawable.lmini3,
				R.drawable.lmini4,
				R.drawable.lmini5
			};

	private static final int[] rightIds = {
				-1,
				R.drawable.rmini2,
				R.drawable.rmini3,
				R.drawable.rmini4,
				R.drawable.rmini5
			};

	

	public Customer(Context context, int type) {
		this.type = type;
		floor = 1 + (int)(Math.random() * (Elevate.FLOORS - 1));
		age = 0;
		
		int id = 0;
		switch (type) {
		case Elevator.LEFT:
			id  = leftIds[floor];
			break;
		case Elevator.RIGHT:
			id = rightIds[floor];
			break;
		}
		if (-1 == id) {
			throw new AndroidRuntimeException("wrong floor id: "+floor);
		}
		icon = context.getResources().getDrawable(id);
		icon.setBounds(0, 0, Elevate.getScaledWidth(icon), Elevate.getScaledHeight(icon));
//		Log.d("Elevate::Customer", "type: "+String.valueOf(type)+" floor: "+String.valueOf(floor));
	}
	
	
	/**
	 * @return the floor
	 */
	public int getFloor() {
		return floor;
	}
	
	
	/**
	 * @return the wait
	 */
	public int getAge() {
		return age;
	}
	
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * 
	 * @return
	 */
	public Drawable getDrawable() {
		return icon;
	}


	/**
	 * @return 
	 * 
	 */
	public int timeTick() {
		return ++age;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Customer(f=" + floor + ", a=" + age + ")";
	}
}