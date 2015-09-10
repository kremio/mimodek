package mimodek2.graphics;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix2D;
import processing.core.PVector;

public class Navigation {

	public static PVector zoom;
	private static float speedFactor = 0.1f;
	private static float maxSpeed = 1f;
	private static float maxZoom = 10f;
	
	
	public static void setup(){
		zoom = new PVector(0, 0, 1f);
	}
	
	public static void setZoomCenter(float sx, float sy){
		
		PMatrix2D matrix = new PMatrix2D();
		matrix.translate(zoom.x , zoom.y);
		matrix.scale( 1f/zoom.z );
		matrix.translate(-zoom.x, -zoom.y);
		
		float[] s = {sx, sy};
		
		s = matrix.mult( s, new float[2] );

		zoom.x = s[0];
		zoom.y = s[1];
	}
	
	public static void zoom(float sx, float sy, float dx, float dy){
		float speed = PApplet.dist(sx, sy, dx, dy);
		if( sy  > dy ){
			zoom.z *= 1 + speed * speedFactor * maxSpeed;
		}else{
			zoom.z /= 1 + speed * speedFactor * maxSpeed;
		}
		
		zoom.z = PApplet.min(zoom.z, maxZoom);
		
	}
	
	
	public static void resetZoom(){
		zoom = new PVector(0, 0, 1f);
	}
	
	public static void applyTransform(PGraphics graphics){
		graphics.translate(zoom.x , zoom.y);
		graphics.scale( zoom.z );
		graphics.translate(-zoom.x, -zoom.y);
	}

}

