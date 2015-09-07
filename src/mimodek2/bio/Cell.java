package mimodek2.bio;

/*
This is the code source of Mimodek. When not stated otherwise,
it was written by Jonathan 'Jonsku' Cremieux<jonathan.cremieux@aalto.fi> in 2010. 
Copyright (C) yyyy  name of author

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

/**
 * The Class Cell.
 */
public class Cell{
	 

	public long id;
	
	/** The pos. */
 	public PVector pos;
	 
 	/** The maturity. */
 	public float maturity = 0;
	 
 	/** The current maturity. */
 	public float currentMaturity = 0;
	 
 	/** The anchor. */
 	public Cell anchor = null;
	
	/** The angle to anchor. */
	public float angleToAnchor = 0;

	/** The z level. */
	public float zLevel = 0;
	
	/** The last update. */
	public long lastUpdate;
	
	public static PShape shape;
	
	public static long nextId = 0;
	 
	  /**
  	 * Instantiates a new cell.
  	 *
  	 * @param pos the pos
  	 */
  	public Cell(PVector pos){
	    this.pos = pos;
	    lastUpdate = System.currentTimeMillis();
	    this.id = nextId++;
	  }
	  
	 /**
  	 * Sets the anchor.
  	 *
  	 * @param anchor the new anchor
  	 */
  	void setAnchor(Cell anchor){
	    this.anchor = anchor;
	    angleToAnchor = PApplet.atan2(pos.y-anchor.pos.y,pos.x-anchor.pos.x);
	  }
	  
	  /**
  	 * Update postion and other parameters.
  	 *
  	 * @param app the app
  	 */
  	public void update(PApplet app){
  		return;
  	}
	 
	 
	 /**
 	 * Feed.
 	 *
 	 * @param amount the amount to feed the cell
 	 */
 	void feed(float amount){
	   maturity+=amount;
	 }
	 
	 /**
 	 * Radius.
 	 *
 	 * @return the radius of the cell
 	 */
 	public float radius(){
		 return 0;
	 }
 	
 	public HashMap<String, Object> getState(){
 		HashMap<String, Object> state = new HashMap<String, Object>();
 		state.put("id", id);
 		state.put("posX", pos.x);
 		state.put("posY", pos.y);
 		state.put("maturity", maturity);
 		state.put("currentMaturity", currentMaturity);
 		if( anchor != null ){
 			state.put("anchor", anchor.id);
 		}
 		state.put("angleToAnchor", angleToAnchor);
 		state.put("zLevel", zLevel);
 		return state;
 	}
 	
 	public void link(HashMap<Long, Cell> cells, HashMap<String, Object> state){
 		Class<?> thisClass = this.getClass();
		Set<String> keys = state.keySet();
		Field theField;
		for(String key : keys){
			try{
				//If the field is found
				theField = thisClass.getField( key );
				
				if( theField.getType().getSimpleName().equalsIgnoreCase("Cell") || ( theField.getType().getSuperclass() != null && theField.getType().getSuperclass().getSimpleName().equalsIgnoreCase("Cell") ) ){
					//set its value
					theField.set(this, cells.get( state.get(key) ) );
				}
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}
 	}
 	
	public void setState(HashMap<String, Object> state){
		
		//Deal with vectors
		if( state.containsKey("posX") && state.containsKey("posY") ){
			this.pos = new PVector(Float.class.cast( state.get("posX") ), Float.class.cast( state.get("posY") ));
			state.remove("posX");
			state.remove("posY");
		}
		
		Class<?> thisClass = this.getClass();
		Set<String> keys = state.keySet();
		Field theField;
		for(String key : keys){
			try{
				//If the field is found
				theField = thisClass.getField( key );
				
				if( theField.getType().getSimpleName().equalsIgnoreCase("Cell") || ( theField.getType().getSuperclass() != null && theField.getType().getSuperclass().getSimpleName().equalsIgnoreCase("Cell") ) ){
					//This field requires linking object, skip for now
					continue;
				}
				
				//set its value
				theField.set(this, state.get(key) );
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		

	}
}
