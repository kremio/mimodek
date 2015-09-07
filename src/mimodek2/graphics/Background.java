package mimodek2.graphics;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class Background {
	
	public static PGraphics background;
	public static float scaleAmount = 0.01f;
	private static int topOffset;
	private static int leftOffset;
	private static int scaleWidth;
	private static int scaleHeight;
	
	public static void setup(PApplet app, int fillColor){
		background = app.createGraphics(app.width, app.height, PApplet.P3D);
		background.beginDraw();
		background.background(0,0);
		background.endDraw();
		
		scaleWidth = PApplet.round(app.width * (1 + scaleAmount) );
		leftOffset = PApplet.round( (scaleWidth - app.width) / 2 );
		
		scaleHeight = PApplet.round(app.height * (1 + scaleAmount) );
		topOffset = PApplet.round( (scaleHeight - app.height) / 2 );
	}
	
	public static void addToBackground(PImage image){
		PImage previous = background.get();
		background.beginDraw();
		background.background(0,0.0001f);
		background.imageMode(PApplet.CENTER);
		background.translate(background.width/2f, background.height/2f);
		background.pushMatrix();
		background.scale(1f+scaleAmount);
		//background.tint(0f, 1f);
		background.image(previous,0,0);
		
		//background.copy(0, 0, background.width, background.height, -leftOffset, -topOffset, scaleWidth, scaleHeight);
		background.filter(PApplet.BLUR);
		background.popMatrix();
		
		background.image(image, 0, 0);
		
		background.endDraw();
	}
	
	public static PImage get(){
		return background;
	}
	
	public static void render(PImage source){
		
	
	}
	
}
