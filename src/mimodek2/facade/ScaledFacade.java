package mimodek2.facade;

import processing.core.PApplet;
import processing.core.PVector;

public class ScaledFacade extends Facade {
	
	float hScale = 1;
	float wScale = 1;
	float xTrans = 0;
	float yTrans = 0;
	
	public ScaledFacade(PApplet app, int width, int height){
		super(app);
		this.width = width;
		this.height = height;
		
		halfWidth = width / 2;
		halfHeight = height / 2;
		
		xTrans = ((float)app.width - (float)width) * 0.5f;
		yTrans = ((float)app.height - (float)height) * 0.5f;
		
		wScale = (float)app.width/(float)width;
		hScale = (float)app.height/(float)height;
	}
	
	public void pre(){
		//Scale so that the drawing area fits the window
		//app.translate(xTrans, yTrans);
		app.scale(wScale, hScale);
		//app.translate(width*0.5f, height*0.5f);
		app.noFill();
		app.strokeWeight(5);
		app.stroke(1f);
		app.rect(0, 0, width, height);
	}
	
	@Override
	public void draw() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see mimodek.facade.Facade#isInTheScreen(processing.core.PVector, float)
	 */
	@Override
	public boolean isInTheScreen(PVector coordinate, float margin) {
		return isInTheScreen(coordinate.x, coordinate.y, margin);
	}

	/* (non-Javadoc)
	 * @see mimodek.facade.Facade#isInTheScreen(processing.core.PVector)
	 */
	@Override
	public boolean isInTheScreen(PVector coordinate) {
		return isInTheScreen(coordinate.x, coordinate.y);
	}

	/* (non-Javadoc)
	 * @see mimodek.facade.Facade#isInTheScreen(float, float, float)
	 */
	@Override
	public boolean isInTheScreen(float x, float y, float margin) {
		margin+=border;
		return x>=margin && x<width-margin && y>=margin && y<height-margin;
	}

	/* (non-Javadoc)
	 * @see mimodek.facade.Facade#isInTheScreen(float, float)
	 */
	@Override
	public boolean isInTheScreen(float x, float y) {
		return isInTheScreen(x, y, 0);
	}

}
