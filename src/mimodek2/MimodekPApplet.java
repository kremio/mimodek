package mimodek2;

import mimodek2.bio.Food;
import mimodek2.facade.FacadeFactory;
import mimodek2.graphics.Navigation;
import processing.core.*;



@SuppressWarnings("serial")
public class MimodekPApplet extends PApplet{

	private Mimodek mimodek;

	public static void main(String args[]) {
		Configurator.createConfigurator( new PApplet() );
		Configurator.loadFromFile("settings/settings.xml");
		Verbose.speak = Configurator.getBooleanSetting("DEBUG_FLAG");
		
		if( Configurator.getBooleanSetting("FULLSCREEN_FLAG") )
			PApplet.main(new String[] { "--present", "mimodek2.MimodekPApplet" });
		else
			PApplet.main(new String[] { "mimodek2.MimodekPApplet" });
	}

	public void setup() {
		if(Configurator.getBooleanSetting("FULLSCREEN_FLAG"))
			((javax.swing.JFrame) frame).getContentPane().setBackground(new java.awt.Color(0,0,0));
		size(Configurator.getIntegerSetting("WINDOW_WIDTH_INT"), Configurator.getIntegerSetting("WINDOW_HEIGHT_INT"), P3D);
		
		// Writing to the depth buffer is disabled to avoid rendering
		// artifacts due to the fact that the particles are semi-transparent
		// but not z-sorted.
		//hint(DISABLE_DEPTH_MASK);
		
		mimodek = new Mimodek((PApplet)this, Configurator.getIntegerSetting("FACADE_TYPE_IN"));
		
	}

	public void draw() {
		mimodek.callAfterRender();
		background( Configurator.getBooleanSetting("FULLSCREEN_FLAG") ? 0 :.33f );
		mimodek.update();
		mimodek.draw();
	}
	
	float mouseIntX;
	float mouseIntY;
	boolean zoomOn = false;
	boolean translateOn = false;
	
	public void keyPressed(){
		if(key == ' ' && !zoomOn){
			mouseIntX = mouseX;
			mouseIntY = mouseY;
			//Navigation.setZoomCenter(mouseX, mouseY);
			Navigation.setZoomTarget(mouseX, mouseY);
			zoomOn = true;
			translateOn = false;
		}
		
		if(key == CODED){
			if(keyCode == SHIFT){
				zoomOn = false;
				translateOn = true;
			}
		}
		
		if(key == 'r'){
			Navigation.resetZoom();
		}
		
		if(key == 'c'){
			mimodek.jsConsole.openConsole();
		}
		
	}
	
	public void keyReleased(){
		zoomOn = false;
		translateOn = false;
	}
	
	public void mouseRealeased(){
		zoomOn = false;
		translateOn = false;
	}
	
	public void mousePressed(){
		mouseIntX = mouseX;
		mouseIntY = mouseY;
	}
	
	/**
	 * Mouse dragged.
	 */
	public void mouseDragged() {
		
		if(zoomOn){
			Navigation.zoom(pmouseX, pmouseY , mouseX , mouseY);
			return;
		}
		
		if(translateOn){
			Navigation.translate(pmouseX, pmouseY , mouseX , mouseY);
		}
		
		Food.dropFood(FacadeFactory.getFacade().mouseX/Configurator.getFloatSetting("GLOBAL_SCALING") + (-1f + (float) Math.random() * 2),FacadeFactory.getFacade().mouseY/Configurator.getFloatSetting("GLOBAL_SCALING") + (-1f + (float) Math.random() * 2));
	}


}
