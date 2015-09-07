package mimodek2;

import java.lang.reflect.InvocationTargetException;

import mimodek2.bio.Food;
import mimodek2.facade.FacadeFactory;
import mimodek2.graphics.Background;
import mimodek2.graphics.Navigation;
import processing.core.*;



@SuppressWarnings("serial")
public class MimodekPApplet extends PApplet{
	
	
	
	private Mimodek mimodek;

	public static void main(String args[]) {
		PApplet.main(new String[] { /*"--present",*/ "mimodek2.MimodekPApplet" });
	}

	public void setup() {
		size(960,540, P3D);
		
		  // Writing to the depth buffer is disabled to avoid rendering
		  // artifacts due to the fact that the particles are semi-transparent
		  // but not z-sorted.
		hint(DISABLE_DEPTH_MASK);
		
		//
		mimodek = new Mimodek((PApplet)this, FacadeFactory.FULL_WINDOW);
		
	}

	public void draw() {
		mimodek.callAfterRender();
		background(0.33f);
		mimodek.update();
		mimodek.draw();
		
	}
	
	float mouseIntX;
	float mouseIntY;
	boolean zoomOn = false;;
	
	public void keyPressed(){
		if(key == ' ' && !zoomOn){
			mouseIntX = mouseX;
			mouseIntY = mouseY;
			Navigation.setZoomCenter(mouseX, mouseY);
			zoomOn = true;
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
	}
	
	public void mouseRealeased(){
		zoomOn = false;
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
				/*
				if(mouseIntY > mouseY){
					//zoom in
					Navigation.zoomIn(mouseIntX / width, mouseIntY / height);
				}else{
					//zoom out
					Navigation.zoomOut(mouseIntX / width, mouseIntY / height);
				}
				*/
				return;
		}
		
		Food.dropFood(FacadeFactory.getFacade().mouseX/Configurator.getFloatSetting("GLOBAL_SCALING") + (-1f + (float) Math.random() * 2),FacadeFactory.getFacade().mouseY/Configurator.getFloatSetting("GLOBAL_SCALING") + (-1f + (float) Math.random() * 2));
		// Food.dropFood(FacadeFactory.getFacade().mouseX,
		// FacadeFactory.getFacade().mouseY);
	}
	
	public void mouseClicked(){
		Background.addToBackground( mimodek.cellsAPass );
	}


}
