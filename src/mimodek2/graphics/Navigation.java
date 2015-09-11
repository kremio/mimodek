package mimodek2.graphics;

import mimodek2.facade.FacadeFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix2D;
import processing.core.PVector;

public class Navigation {

	public static PVector zoom;
	private static float speedFactor = 0.1f;
	private static float maxSpeed = 1f;
	private static float maxZoom = 10f;
	
	private static PVector zoomTarget = new PVector(0, 0);
	
	
	public static void setup(){
		zoom = new PVector(FacadeFactory.getFacade().halfWidth, FacadeFactory.getFacade().halfWidth, 1f);
	}
	
	public static void setZoomCenter(float sx, float sy){
		
		//reverse transform to get world coordinate of window coordinate
		PMatrix2D matrix = new PMatrix2D();
		matrix.translate(zoom.x , zoom.y);
		matrix.scale( 1f/zoom.z );
		matrix.translate(-zoom.x, -zoom.y);
		
		float[] s = {sx, sy};
		
		s = matrix.mult( s, new float[2] );

		zoom.x = s[0];
		zoom.y = s[1];
	}
	
	public static void setZoomTarget(int sx, int sy) {

		PMatrix2D matrix = new PMatrix2D();
		matrix.translate(zoom.x , zoom.y);
		matrix.scale( 1f/zoom.z );
		matrix.translate(-zoom.x, -zoom.y);
		
		float[] s = {sx, sy};
		
		s = matrix.mult( s, new float[2] );
		zoomTarget = new PVector(s[0], s[1]);
	}
	
	public static void translate(float sx, float sy, float dx, float dy) {
			zoomTarget.x += (sx - dx) / zoom.z;
			zoomTarget.y += (sy - dy) / zoom.z;
			zoom(0,0,0,0);
	}
	
	public static void zoom(float sx, float sy, float dx, float dy){
		float speed = PApplet.dist(sx, sy, dx, dy);
		if( sy  > dy ){
			zoom.z *= 1 + speed * speedFactor * maxSpeed;
		}else{
			zoom.z /= 1 + speed * speedFactor * maxSpeed;
		}
		
		zoom.z = PApplet.min(zoom.z, maxZoom);
		
		//zoom towards zoom target
		if( zoom.z > 1f ){
			float delta = (zoom.z-1f)/(maxZoom-1f);
			zoom.x = PApplet.lerp(FacadeFactory.getFacade().halfWidth, zoomTarget.x, delta);
			zoom.y = PApplet.lerp( FacadeFactory.getFacade().halfHeight, zoomTarget.y, delta);
		}else{
			zoom.x = FacadeFactory.getFacade().halfWidth;
			zoom.y = FacadeFactory.getFacade().halfHeight;
		}
		
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

