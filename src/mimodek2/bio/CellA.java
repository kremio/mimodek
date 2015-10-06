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

import java.util.HashMap;

import mimodek2.*;
import mimodek2.data.*;
import mimodek2.facade.FacadeFactory;
import mimodek2.graphics.Tween;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;

// TODO: Auto-generated Javadoc
/**
 * The Class CellA.
 */
public class CellA extends Cell {
	
	
	public static final float MIN_DEPTH = 1f;
	public static final float MAX_DEPTH = 2f + MIN_DEPTH;
	
	/** The level. */
	public float level = 1f;
	
	protected static Tween maxLevelTween = new Tween(CellA.class, "maxLevel", 1f, 1000);
	
	/** The max level. */
	public static float maxLevel = 1f;
	
	/** The maxz level. */
	public static float maxzLevel = 0f;
	
	/** The minz level. */
	public static float minzLevel = 0f;
	
	/** The radius modifier. */
	public float radiusModifier = 1f;
	
	/** The angular movement. */
	public float angularMovement = 0;
	
	/** The aa. */
	public float aa = 0;
	
	/** The z level slope. */
	public float zLevelSlope = 1f;
	
	public int leavesCount;
	
	/** The temperature interpolator. */
	public static DataInterpolator temperatureInterpolator;
	
	/** The humidity interpolator. */
	public static DataInterpolator humidityInterpolator;
	
	public static PImage texture;
	
	public HashMap<String, Object> getState(){
		HashMap<String, Object> state = super.getState();
		
		state.put("level", level);
		state.put("radiusModifier", radiusModifier);
		state.put("angularMovement", angularMovement );
		state.put("aa", aa );
		state.put("zLevelSlope", zLevelSlope );
		
		return state;
	}
	
	public static void createShape(PGraphics renderBuffer, PImage texture) {
		CellA.texture = texture;
		int corners = 13;
		PShape pshape = renderBuffer.createShape();
		pshape.beginShape(PApplet.TRIANGLE_FAN);
		pshape.noStroke();

		// Center vertex
		pshape.vertex(0f, 0f, 0.5f, 0.5f);

		float angle = PApplet.TWO_PI / corners;
		for (float i = 0; i < corners; i += 1.0f) {
			pshape.vertex(PApplet.cos(angle * i), PApplet.sin(angle * i),
					0.5f + PApplet.cos(angle * i) * 0.5f, 0.5f + PApplet.sin(angle * i) * 0.5f);
		}

		// close the triangle fan
		pshape.vertex(1f, 0f, 1f, 0.5f);

		pshape.endShape();
		pshape.setTexture(texture);
		
		CellA.shape = pshape;
		
	}
	
	/**
	 * Instantiates a new cell a.
	 *
	 * @param pos the pos
	 * @param color the color
	 */
	public CellA(PVector pos, PVector color) {
		super(pos);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new cell a.
	 *
	 * @param pos the pos
	 * @param radius the radius
	 */
	public CellA(PVector pos, float radius) {
		this(pos, new PVector((float) Math.random(), (float) Math.random(),
				(float) Math.random()));
		this.radiusModifier = radius;
	}

	
	/* (non-Javadoc)
	 * @see MimodekV2.Cell#setAnchor(MimodekV2.Cell)
	 */
	@Override
	void setAnchor(Cell anchor){
		super.setAnchor(anchor);
		level = ((CellA)anchor).level;
		//This tween should be slower that the max level tween to track it properly
		new Tween(this, "level", level+1f, maxLevelTween.duration + 1000);
		zLevel = anchor.zLevel+((CellA)anchor).zLevelSlope;
		
		maxzLevel = PApplet.max(maxzLevel,zLevel);
		minzLevel = PApplet.min(minzLevel,zLevel);
		zLevelSlope = ((CellA)anchor).zLevelSlope;
		((CellA)anchor).zLevelSlope*=-1f;
		if( maxLevelTween.targetValue < level+1 )
			maxLevelTween.restart( level+1 );
		//maxLevel = ;
		angularMovement = (float) (Math.random() * (PConstants.HALF_PI / 10f));
		angularMovement *= Math.random() > 0.5 ? 1f : -1f;
	}
	
	
	/* (non-Javadoc)
	 * @see MimodekV2.Cell#radius()
	 */
	@Override
	public float radius() {
		// TODO Auto-generated method stub
		return Configurator.getFloatSetting("CELLA_RADIUS")*radiusModifier;
	}
	
	public float depth(){
		return MIN_DEPTH + (level / maxLevel) * (MAX_DEPTH - MIN_DEPTH);
	}
	
	/* (non-Javadoc)
	 * @see MimodekV2.Cell#update(processing.core.PApplet)
	 */
	@Override
	public void update(PApplet app) {
		if(anchor!=null){
			//apply angular movement
			aa = angleToAnchor + angularMovement* PApplet.sin(PApplet.radians(app.frameCount + level));
			float distanceBetween = 0.42f+(humidityInterpolator.getInterpolatedValue()/100f)*(0.75f-0.42f);
			pos.x = anchor.pos.x + (radius()+anchor.radius())* distanceBetween * PApplet.cos(aa);
			pos.y = anchor.pos.y + (radius()+anchor.radius())* distanceBetween * PApplet.sin(aa);
		}
		pos.z = depth();
		
		if (currentMaturity < maturity)
			currentMaturity += 0.01;
	}

	public static CellA getRandomCell(){
		return Mimodek.aCells.get( (int)Math.floor(Math.random() * Mimodek.aCells.size() ) );
	}
	
	/**
	 * Try to add a new cell to the structure.
	 * The algorithm will randomly calculate a position for the new cell
	 * based on a randomly picked anchor cell.
	 * It will then test if the new cell would appear inside the frame or overlap with an existing cell.
	 * The number of times the algorithm is allowed to run before failing is controlled
	 * by the "CELLA_MAX_TRY_INT" Configurator setting.
	 *
	 * 
	 * @return the new newly created cell or null if all attempts failed.
	 */
	public static CellA addCellA() {
		/*
		 * Controls the distance between a cell and its anchor.
		 * The value is factor of the anchor's radius.
		 * 0: new cell and anchor overlap
		 * 1: new cell sits one anchor radius away from the anchor
		 */
		float dTo = 0.75f;//0.42f+(humidityInterpolator.getInterpolatedValue()/100f)*(0.75f-0.42f);
		//float dTo = Configurator.getFloatSetting("CELLA_DISTANCE_BETWEEN");
		
		//Radius of the new cell
		float radius = Configurator.getFloatSetting("CELLA_RADIUS");
		
		/*
		 * Modify the radius (and thus the position) of the new cell based on
		 * its distance to the root cell.
		 * The farther the smaller.
		 */
		float radiusModifier = 1f;
		
		CellA newCell = null;
		
		//The cell the new cell will be anchored to 
		CellA anchor;
		
		//Root cell, used to modulate the size of the cells
		CellA root;
		
		//Distance of the root cell from the origin
		float maxD;
		
		//The angle of attachment to the anchor
		float a;
		
		//The position of the new cell
		PVector pos;
		
		//True if the new cell passes all the validation steps
		boolean addIt = false;
		
		//Count the number of loop iterations
		int counter = 0;
		int maxTry = Configurator.getIntegerSetting("CELLA_MAX_TRY_INT");
		
		do{
			counter++;
			
			//Pick a random cell 
			anchor = getRandomCell();
			
			if( anchor.level < 1.0f)
				continue;
			
			//Root cell, used to modulate the size of the cells
			root = anchor.getRootCell();
			
			//Distance of the root cell from the origin
			maxD = root.pos.dist(new PVector(0, 0));
			
			//Randomly choose the angle of attachment
			a = (float) Math.random() * PConstants.TWO_PI;
			
			pos = new PVector(anchor.pos.x + PApplet.cos(a)
					* anchor.radius() * dTo, anchor.pos.y + PApplet.sin(a)
					* anchor.radius() * dTo);
			
			//Modify the radius of the new cell based on its distance from the root
			radiusModifier = (maxD - pos.dist(root.pos)) / maxD;
			
			//The position of the new cell
			pos = new PVector(anchor.pos.x + PApplet.cos(a)
					* (anchor.radius() + radius*radiusModifier)* dTo, anchor.pos.y + PApplet.sin(a)
					* (anchor.radius() + radius*radiusModifier)* dTo);
			
			//Will the new cell be inside the frame ?
			if (!FacadeFactory.getFacade().isInTheScreen(pos, radius*radiusModifier)) {
				//try again!
				continue;
			}
			
			addIt = true;
			//Will the new cell overlap with an existing cell?
			for (Cell toTest: Mimodek.allCells) {
				//Don't include the anchor in the test
				if (toTest == anchor /*|| (toTest instanceof CellB && ((CellB)toTest).eatable)*/)
					continue;
				
				if (toTest.pos.dist(pos) < (radius*radiusModifier + toTest.radius()*dTo )) {
					addIt = false;
					break;
				}
			}
			
			if(!addIt){
				//try again!
				continue;
			}
			
			newCell = new CellA(pos, radiusModifier);
			newCell.setAnchor(anchor);
			
		}while(newCell == null && counter < maxTry);
		
		
		return newCell;
	}
	
	public CellA getRootCell() {
		
		if(anchor == null){
			return this;
		}
		
		//visit next node
		return ((CellA)anchor).getRootCell();
	}
	
	public boolean updateLevel(CellA rootCell){
		if( anchor == null)
			return false;
		
		if( anchor.id == rootCell.id || ((CellA)anchor).updateLevel(rootCell)){
			if(anchor.id == rootCell.id)
				anchor = null;
			new Tween(this, "level", level-1f, maxLevelTween.duration + 1000);
			return true;
		}
		
		return false;
		
		//level += incLevel;
		/*
		if( level == 0 )
			anchor = null;
		
		return incLevel;
		*/
	}
	
	
	/**
	 * 
	 * Find the lowest cells and removes them.
	 * Returns the list of removed cells.
	 * 
	 */
	public static CellA unRootCells(){

		//Find the root cell of a cell picked randomly
		CellA rootCell = getRandomCell().getRootCell();
		new Tween(rootCell, "level", 0.5f, maxLevelTween.duration + 1000);
		Mimodek.cellsToFossilize.add(rootCell);
		
		//Update the levels
		float max = 0;
		for(CellA cellA : Mimodek.aCells){
			if( cellA.updateLevel(rootCell) ){
				max = Math.max(cellA.level-1f, max);
			}else{
				max = Math.max(cellA.level, max);
			}
			//
		}
		
		maxLevelTween.restart( max );
		
		//CellA.maxLevel = (int)max;
		
		return rootCell;
	}

	public void addLeaf() {
		leavesCount++;
	}
	
	public void removeLeaf() {
		leavesCount--;
	}
	
	public boolean canAddLeaf(){
		return leavesCount < Configurator.getIntegerSetting("CELLA_MAX_LEAVES_INT");
	}


}
