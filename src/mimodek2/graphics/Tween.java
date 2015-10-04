package mimodek2.graphics;

import java.lang.reflect.*;
import java.util.ArrayList;

public class Tween {

	public static ArrayList<Tween> activeTweens = new ArrayList<Tween>();
	
	public static void updateTweens(){
		for(int t=0; t < activeTweens.size(); t++){
			try {
				activeTweens.get(t).tween();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	Object object;
	
	float sourceValue;
	public float targetValue;
	public long duration;
	long startTime = -1;
	
	boolean done;
	
	int tweenIndex;
	Field propertyField;
	
	public Tween(Object object, String propertyName, float targetValue, long duration) {
		try {
			this.object = object;
			this.duration = duration;
			propertyField = object.getClass().getField(propertyName);
			sourceValue = propertyField.getFloat(object);
			this.targetValue = targetValue;
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		done = false;
		activeTweens.add(this);

	}
	
	public Tween(Class<?> staticClass, String propertyName, float targetValue, long duration) {
		try {
			object = null;
			this.duration = duration;
			propertyField = staticClass.getField(propertyName);
			sourceValue = propertyField.getFloat(object);
			this.targetValue = targetValue;
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		done = false;
		activeTweens.add(this);

	}
	
	public boolean tween() throws IllegalArgumentException, IllegalAccessException{
		if(done)
			return true;
		startTime = startTime >= 0 ? startTime : System.currentTimeMillis();
		float position = Math.min(1.0f, ((float)(System.currentTimeMillis() - startTime))/(float)duration );
		float tweenVal = sourceValue + position * (targetValue - sourceValue);
		done = Math.abs(tweenVal-targetValue) <= 0.001f;
		if( done ){
			tweenVal = targetValue;
			//System.out.println("Tween " + propertyField.getName() + " done.");
			activeTweens.remove(this);
		}
		
		//System.out.println(propertyField.getName()+": " +tweenVal+" to "+targetValue);
		propertyField.set(object, tweenVal);
		return done;
	}
	
	public void restart(float targetValue){
		startTime = -1;
		done = false;
		this.targetValue = targetValue;
		if( !activeTweens.contains(this) )
			activeTweens.add(this);
		try {
			sourceValue = (Float)propertyField.get(object);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
