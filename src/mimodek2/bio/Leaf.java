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

import java.util.ArrayList;
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
 * The Class CellB.
 */
public class Leaf extends Cell {

	/** The use pollution data. */
	public static boolean usePollutionData = true;

	/** The pollution interpolator. */
	public static PollutionDataInterpolator pollutionInterpolator;

	/** The temperature interpolator. */
	public static DataInterpolator temperatureInterpolator;

	/** The next offset angle. */
	public float nextOffsetAngle = 0;

	/** The current angle. */
	public float currentAngle = 0;

	/** The current brightness. */
	public float currentBrightness = 1f;

	/** The next offset brightness. */
	public float nextOffsetBrightness = 0.5f;

	/** The distance modifier. */
	public float distanceModifier = 1f;

	/** The moving. */
	public boolean moving = false;

	/** The eatable. */
	public boolean edible = false;

	/** The creature b. */
	public Lightie creatureA;

	public Lightie creatureB;

	/** The drop me at a. */
	PVector dropMeAtA;

	/** The drop me at b. */
	PVector dropMeAtB;

	/** The color. */
	public int color;

	public static PImage texture;

	public HashMap<String, Object> getState() {
		HashMap<String, Object> state = super.getState();

		state.put("nextOffsetAngle", nextOffsetAngle);
		state.put("currentAngle", currentAngle);
		state.put("currentBrightness", currentBrightness);
		state.put("nextOffsetBrightness", nextOffsetBrightness);
		state.put("distanceModifier", distanceModifier);
		state.put("moving", moving);
		state.put("eatable", edible);
		state.put("color", color);

		if (creatureA != null)
			state.put("creatureA", creatureA.id);

		if (creatureB != null)
			state.put("creatureB", creatureB.id);

		if (dropMeAtA != null) {
			state.put("dropMeAtAX", dropMeAtA.x);
			state.put("dropMeAtAY", dropMeAtA.y);
		}

		if (dropMeAtB != null) {
			state.put("dropMeAtBX", dropMeAtB.x);
			state.put("dropMeAtBY", dropMeAtB.y);
		}

		if (!moving && edible && anchor != null) {
			state.remove("anchor");
			state.put("dropX", anchor.pos.x);
			state.put("dropY", anchor.pos.y);
		}

		return state;
	}

	public void setState(HashMap<String, Object> state) {

		// Deal with vectors
		if (state.containsKey("dropMeAtAX") && state.containsKey("dropMeAtAY")) {
			this.dropMeAtA = new PVector(Float.class.cast(state.get("dropMeAtAX")), Float.class.cast(state
					.get("dropMeAtAY")));
			state.remove("dropMeAtAX");
			state.remove("dropMeAtAY");
		}
		if (state.containsKey("dropMeAtBX") && state.containsKey("dropMeAtBY")) {
			this.dropMeAtB = new PVector(Float.class.cast(state.get("dropMeAtBX")), Float.class.cast(state
					.get("dropMeAtBY")));
			state.remove("dropMeAtBX");
			state.remove("dropMeAtBY");
		}

		// Dropped/dead leaf
		PVector droppedAnchor = null;
		if (state.containsKey("dropX") && state.containsKey("dropY")) {
			droppedAnchor = new PVector(Float.class.cast(state.get("dropX")), Float.class.cast(state.get("dropY")));
			state.remove("dropX");
			state.remove("dropY");
			state.remove("anchor");
		}

		super.setState(state);

		if (droppedAnchor != null) {
			setAnchor(new Cell(droppedAnchor));
		}

	}

	public static void createShape(PGraphics renderBuffer, PImage texture) {

		Leaf.texture = texture;
		PShape pshape = renderBuffer.createShape();
		pshape.beginShape();
		pshape.noStroke();

		pshape.vertex(-1f, -1f, 0f, 0f);
		pshape.vertex(1f, -1f, 0f, 1f);
		pshape.vertex(1f, 1f, 1f, 1f);
		pshape.vertex(-1f, 1f, 1f, 0f);
		pshape.endShape(PApplet.CLOSE);
		pshape.setTexture(texture);

		Leaf.shape = pshape;

	}

	/**
	 * Instantiates a new cell b.
	 *
	 * @param pos
	 *            the pos
	 * @param distanceModifier
	 *            the distance modifier
	 */
	public Leaf(PVector pos, float distanceModifier) {
		super(pos);
		this.distanceModifier = distanceModifier;
	}

	/**
	 * Make the leaf edible
	 */
	public void makeEdible() {
		if (!edible && currentMaturity >= 1f && creatureA == null && creatureB == null) {
			call2Creatures();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MimodekV2.Cell#radius()
	 */
	@Override
	public float radius() {
		// TODO Auto-generated method stub
		return Configurator.getFloatSetting("CELLB_RADIUS");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MimodekV2.Cell#setAnchor(MimodekV2.Cell)
	 */
	@Override
	void setAnchor(Cell anchor) {
		super.setAnchor(anchor);
		currentAngle = angleToAnchor;
		nextOffsetAngle = angleToAnchor;
	}

	/**
	 * Call2 creatures.
	 */
	void call2Creatures() {
		int count = 0;
		// one from the existing ones
		int indA = PApplet.round((float) Math.random() * (Mimodek.lighties.size() - 1));
		while (Mimodek.lighties.get(indA).cellB != null && count < 50) {
			count++;
			indA = PApplet.round((float) Math.random() * (Mimodek.lighties.size() - 1));
		}
		if (count >= 50)
			return;

		creatureA = Mimodek.lighties.get(indA);
		creatureA.cellB = this;

		// and an fresh new one
		creatureB = Lightie.spawn();
		creatureB.cellB = this;
	}

	/**
	 * Drop.
	 */
	void drop() {
		moving = false;
		edible = true;
		pos = new PVector(creatureA.pos.x, creatureA.pos.y);
		setAnchor(new Cell(new PVector(creatureB.pos.x, creatureB.pos.y)));
		creatureA.cellB = null;
		creatureB.cellB = null;
		creatureA = null;
		creatureB = null;
		dropMeAtA = null;
		dropMeAtB = null;
	}

	/**
	 * Gets the creature target position.
	 *
	 * @param c
	 *            the c
	 * @return the creature target position
	 */
	PVector getCreatureTargetPosition(Lightie c) {
		if (creatureA == null || creatureB == null)
			return null;
		
		if( c.id != creatureA.id && c.id != creatureB.id )
			return null;
		
		if (creatureA.readyToLift && creatureB.readyToLift) {
			if (dropMeAtA == null && dropMeAtB == null) {
				// find a place to dump the b cell far away from the center
				float a = 0f;
				float d = 0f;
				while (dropMeAtA == null || !FacadeFactory.getFacade().isInTheScreen(dropMeAtA)) {
					d = (float) Math.random() * 100f;// FacadeFactory.getFacade().width<FacadeFactory.getFacade().height?FacadeFactory.getFacade().height/2f:FacadeFactory.getFacade().width/2f;
					a = currentAngle + (-PApplet.TWO_PI * 0.5f + (float) Math.random() * PApplet.TWO_PI);
					// a = (float)Math.random()*PConstants.TWO_PI;
					dropMeAtA = new PVector(FacadeFactory.getFacade().width / 2f + d * PApplet.cos(a),
							FacadeFactory.getFacade().height / 2f + d * PApplet.sin(a), CellA.MIN_DEPTH);
				}
				a = (float) Math.random() * PConstants.TWO_PI;
				d = getSize();
				dropMeAtB = new PVector(dropMeAtA.x + d * PApplet.cos(a), dropMeAtA.y + d * PApplet.sin(a),
						CellA.MIN_DEPTH);
			}
			
			moving = true;
			return c.id == creatureA.id ? dropMeAtA : dropMeAtB;

		}
		
		if (c.id == creatureA.id) { // creature A goes to the top
			return new PVector(anchor.pos.x + currentMaturity * (pos.x - anchor.pos.x), anchor.pos.y + currentMaturity
					* (pos.y - anchor.pos.y), anchor.pos.z);
		} else { // creature B goes at the bottom
			return new PVector(anchor.pos.x + 0.4f * (pos.x - anchor.pos.x), anchor.pos.y + 0.4f
					* (pos.y - anchor.pos.y), anchor.pos.z);
		}
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public float getSize() {
		float minDistanceToA = Configurator.getFloatSetting("CELLB_MIN_DISTANCE_TO_A");
		float maxDistanceToA = Configurator.getFloatSetting("CELLB_MAX_DISTANCE_TO_A") - minDistanceToA;
		return radius() + minDistanceToA + distanceModifier * maxDistanceToA;
	}

	
	public boolean isLeafOffCellA(){
		if(edible || moving)
			return false;
		
		return anchor instanceof CellA;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see MimodekV2.Cell#update(processing.core.PApplet)
	 */
	@Override
	public void update(PApplet app) {
		float minDistanceToA = Configurator.getFloatSetting("CELLB_MIN_DISTANCE_TO_A");
		float maxDistanceToA = Configurator.getFloatSetting("CELLB_MAX_DISTANCE_TO_A") - minDistanceToA;
		if (edible) {
			maturity -= 0.001f;
			if (maturity <= 0f) {
				Mimodek.leavesCells.remove(this);
				Mimodek.allCells.remove(this);
				return;
			}
		} else {

			if (PApplet.abs(currentMaturity - maturity) > 0.01f) {
				currentMaturity += currentMaturity < maturity ? 0.01f : -0.01f;
				currentMaturity = currentMaturity > maturity ? maturity : currentMaturity;
			} else {
				currentMaturity = maturity;
			}
		}

		if (creatureA == null && creatureB == null && !edible) {
			for (int c = 0; c < Mimodek.lighties.size(); c++) {
				Lightie cr = Mimodek.lighties.get(c);
				if (cr.pos.dist(pos) < 20) {

					if (PApplet.abs(currentAngle - nextOffsetAngle) < (PConstants.PI / 180.0f)) {
						nextOffsetAngle = (float) (angleToAnchor - PConstants.PI / 8.0 + (float) Math.random()
								* (PConstants.PI / 4.0));
					} else {
						currentAngle += nextOffsetAngle > currentAngle ? (PConstants.PI / 540.0f)
								: -(PConstants.PI / 540.0f);
					}
					break;
				}
			}
		} else if (moving) {
			currentAngle = PApplet.atan2(creatureA.pos.y - anchor.pos.y, creatureA.pos.x - anchor.pos.x);
		}

		pos.x = anchor.pos.x + PApplet.cos(currentAngle)
				* (radius() + minDistanceToA + distanceModifier * maxDistanceToA);
		pos.y = anchor.pos.y + PApplet.sin(currentAngle)
				* (radius() + minDistanceToA + distanceModifier * maxDistanceToA);

		/*
		 * else if(){ pos.x = anchor.pos.x + PApplet.cos(currentAngle) *
		 * (radius()+minDistanceToA+distanceModifier*maxDistanceToA); pos.y = anchor.pos.y + PApplet.sin(currentAngle) *
		 * (radius()+minDistanceToA+distanceModifier*maxDistanceToA); }
		 */

		if (!edible) {
			if (!usePollutionData) {
				color = TemperatureColorRanges.getColor(TemperatureColorRanges
						.getHigherTemperature(temperatureInterpolator.getInterpolatedValue()));
			} else {
				color = TemperatureColorRanges.getColor(pollutionInterpolator.getInterpolatedValue());
			}

			if (PApplet.abs(nextOffsetBrightness - currentBrightness) < 0.005f) {
				nextOffsetBrightness = Configurator.getFloatSetting("CELLB_ALPHA")
						- (Configurator.getFloatSetting("CELLB_ALPHA") * (float) Math.random() * Configurator
								.getFloatSetting("CELLB_ALPHA_VARIATION"));
			}
			currentBrightness += nextOffsetBrightness > currentBrightness ? 0.005f : -0.005f;
		} else {
			currentBrightness = Configurator.getFloatSetting("CELLB_ALPHA") / 2f;
			if (maturity <= 0.5f) {
				float step = 1f - (maturity / 0.5f);
				currentBrightness = PApplet.lerp(currentBrightness, 0f, step);
			}
		}
	}

	/**
	 * Try to add a new leaf to the structure.
	 * The algorithm will randomly calculate a position for the new leaf
	 * based on a randomly picked anchor cell.
	 * It will then test if the new leaf would appear inside the frame or overlap with an existing cell.
	 * The number of times the algorithm is allowed to run before failing is controlled
	 * by the "CELLB_MAX_TRY_INT" Configurator setting.
	 *
	 * 
	 * @return the new newly created cell or null if all attempts failed.
	 */
	public static Leaf addCellB() {
		float cellA_radius = Configurator.getFloatSetting("CELLA_RADIUS");
		float cellB_radius = Configurator.getFloatSetting("CELLB_RADIUS");
		float minDistanceToA = Configurator.getFloatSetting("CELLB_MIN_DISTANCE_TO_A");
		float maxDistanceToA = Configurator.getFloatSetting("CELLB_MAX_DISTANCE_TO_A") - minDistanceToA;

		Leaf newCell = null;

		// Which cell to attach the new leaf to
		CellA anchor;
		// Angle of attachment to the anchor
		float a;
		// Factor to the default distance between leaf and anchor cell
		float distanceModifier;

		// position of the new leaf
		PVector pos;

		// True if the new leaf passes all the validation steps
		boolean addIt = false;

		// Count the number of loop iterations
		int counter = 0;
		int maxTry = Configurator.getIntegerSetting("CELLB_MAX_TRY_INT");

		do {
			counter++;
			anchor = CellA.getRandomCell();
			a = (float) Math.random() * (PConstants.TWO_PI);
			distanceModifier = (float) Math.random();

			pos = new PVector(anchor.pos.x + PApplet.cos(a)
					* (cellA_radius + cellB_radius + minDistanceToA + distanceModifier * maxDistanceToA), anchor.pos.y
					+ PApplet.sin(a)
					* (cellA_radius + cellB_radius + minDistanceToA + distanceModifier * maxDistanceToA));

			// If the new leaf inside the frame
			if (!FacadeFactory.getFacade().isInTheScreen(pos, cellB_radius)) {
				// Try again!
				continue;
			}

			addIt = true;

			for (Cell toTest : Mimodek.allCells) {

				if (toTest.pos.dist(pos) < (cellB_radius + toTest.radius())
						+ ((toTest instanceof Leaf) ? 0 : minDistanceToA)) { // 3:7
					addIt = false;
					break;
				}
			}

			if (!addIt) {
				// Try again!
				continue;
			}

			newCell = new Leaf(pos, distanceModifier);
			newCell.setAnchor(anchor);

		} while (newCell == null && counter < maxTry);

		return newCell;

	}

	/**
	 * Gets the eatable cell.
	 *
	 * @return the eatable cell
	 */
	public static Leaf getEatableCell() {
		for (Leaf cellB : Mimodek.leavesCells) {
			if (cellB.edible)
				return cellB;
		}
		return null;
	}
	
	/**
	 * IMPORTANT: Call after CellA.unRootCells()
	 * 
	 * Find the lowest leaves and removes them.
	 * Returns the list of removed leaves.
	 * 
	 */
	public static ArrayList<Leaf> unRootLeaves(){
		ArrayList<Leaf> rootCells = new ArrayList<Leaf>();
		//Update the levels and collect all root cells
		for(Leaf cellB : Mimodek.leavesCells){
			if( cellB.isLeafOffCellA() && ((CellA)cellB.anchor).level < 0 ){
				rootCells.add(cellB);
			}
		}
		
		return rootCells;
	}

}
