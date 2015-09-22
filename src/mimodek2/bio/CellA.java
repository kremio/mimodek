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
	
	
	public static final float MIN_DEPTH = 0.7f;
	public static final float MAX_DEPTH = 1f + MIN_DEPTH;
	
	/** The level. */
	public int level = 0;
	
	/** The max level. */
	public static int maxLevel = 1;
	
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
		level = ((CellA)anchor).level+1;
		zLevel = anchor.zLevel+((CellA)anchor).zLevelSlope;
		maxzLevel = PApplet.max(maxzLevel,zLevel);
		minzLevel = PApplet.min(minzLevel,zLevel);
		zLevelSlope = ((CellA)anchor).zLevelSlope;
		((CellA)anchor).zLevelSlope*=-1f;
		maxLevel = PApplet.max(level,maxLevel);
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
		return ((float)level / (float)maxLevel) + MIN_DEPTH;
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

	/**
	 * Adds the cell a.
	 *
	 * @param app the app
	 * @return the cell a
	 */
	public static CellA addCellA(PApplet app) {
		float dTo = 0.75f;//0.42f+(humidityInterpolator.getInterpolatedValue()/100f)*(0.75f-0.42f);
		//float dTo = Configurator.getFloatSetting("CELLA_DISTANCE_BETWEEN");
		float radius = Configurator.getFloatSetting("CELLA_RADIUS");
		Cell root = Mimodek.aCells.get(0);
		float maxD = root.pos.dist(new PVector(0, 0));
		
		int counter = 0;

		CellA added = null;
		//System.out.println("Pick a random aCell:"+PApplet.round((float)Math.random() * (Mimodek.aCells.size() - 1)));
		CellA anchor = Mimodek.aCells.get(PApplet.round((float)Math.random() * (Mimodek.aCells.size() - 1)));
		float a = (float) Math.random() * PConstants.TWO_PI;
		PVector pos = new PVector(anchor.pos.x + PApplet.cos(a) * anchor.radius()
				* dTo, anchor.pos.y + PApplet.sin(a) * anchor.radius() * dTo);
		boolean addIt = false;
		
		float radiusModifier = 1f;
		if (Mimodek.aCells.size() > 0) {

			radiusModifier = (maxD - pos.dist(root.pos)) / maxD;
			pos = new PVector(anchor.pos.x + PApplet.cos(a)
					* (anchor.radius() + radius*radiusModifier)* dTo, anchor.pos.y + PApplet.sin(a)
					* (anchor.radius() + radius*radiusModifier)* dTo);
		}
		while (!addIt && counter < Configurator.getIntegerSetting("CELLA_MAX_TRY_INT")) {
			if (!FacadeFactory.getFacade().isInTheScreen(pos, radius*radiusModifier)) {
				anchor = Mimodek.aCells.get(PApplet.round((float)Math.random() * (Mimodek.aCells.size() - 1)));
				a = (float) Math.random() * PConstants.TWO_PI;
				pos = new PVector(anchor.pos.x + PApplet.cos(a) * anchor.radius()
						* dTo, anchor.pos.y + PApplet.sin(a) * anchor.radius()
						* dTo);
				//radius = Mimodek.CELLA_RADIUS;
				if (Mimodek.aCells.size() > 0) {

					radiusModifier = (maxD - pos.dist(root.pos)) / maxD;
					pos = new PVector(anchor.pos.x + PApplet.cos(a)
							* (anchor.radius() + radius*radiusModifier), anchor.pos.y
							+ PApplet.sin(a) * (anchor.radius() + radius*radiusModifier));
				}
				continue;
			}
			addIt = true;

			for (Cell toTest: Mimodek.theCells) {
				if (toTest == anchor /*|| (toTest instanceof CellB && ((CellB)toTest).eatable)*/)
					continue;
				if (toTest.pos.dist(pos) < (radius*radiusModifier + toTest.radius()*dTo )) {
					addIt = false;
					break;
				}
			}
			
			if (addIt) {
				added = new CellA(pos, radiusModifier);
				added.setAnchor(anchor);
				//TODO:Remove if new animation doesn't work
				//anchor.createNewCell(added);
			} else {
				anchor = Mimodek.aCells.get(PApplet.round((float)Math.random() * (Mimodek.aCells.size() - 1)));
				a = (float) Math.random() * PConstants.TWO_PI;
				pos = new PVector(anchor.pos.x + PApplet.cos(a) * anchor.radius()
						* dTo, anchor.pos.y + PApplet.sin(a) * anchor.radius()
						* dTo);
				//radius = Mimodek.CELLA_RADIUS;
				if (Mimodek.aCells.size() > 0) {

					radiusModifier = (maxD - pos.dist(root.pos)) / maxD;
					pos = new PVector(anchor.pos.x + PApplet.cos(a)
							* (anchor.radius() + radius*radiusModifier)*dTo, anchor.pos.y
							+ PApplet.sin(a) * (anchor.radius() + radius*radiusModifier)*dTo);
				}
			}
			counter++;
		}
		if (counter >= Configurator.getIntegerSetting("CELLA_MAX_TRY_INT")) {
			return null;
		}
		return added;
	}
	
	public CellA getRootCell() {
		
		if(anchor == null || level == 0){
			return this;
		}
		
		//visit next node
		return ((CellA)anchor).getRootCell();
	}
	
	public int updateLevel(){
		if( anchor == null ){
			return level;
		}
		
		int incLevel = ((CellA)anchor).updateLevel();
		level += incLevel;
		
		if( level == 0 )
			anchor = null;
		
		return incLevel;
	}
	
	/**
	 * 
	 * Find the lowest cells and removes them.
	 * Returns the list of removed cells.
	 * 
	 */
	public static CellA unRootCells(){

		//Find the root cell of a cell picked randomly
		CellA rootCell = Mimodek.aCells.get( (int) Math.floor( (Math.random()*Mimodek.aCells.size() ) ) ).getRootCell();
		rootCell.level = -1;
		
		//Update the levels
		float max = 0;
		for(CellA cellA : Mimodek.aCells){
			cellA.updateLevel();
			max = Math.max(cellA.level, max);
		}
		
		CellA.maxLevel = (int)max;
		
		return rootCell;
	}


}
