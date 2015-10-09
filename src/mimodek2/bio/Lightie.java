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
import mimodek2.facade.FacadeFactory;
import processing.core.PApplet;
import processing.core.PVector;

// TODO: Auto-generated Javadoc
/**
 * The Class Creature.
 */
public class Lightie extends Cell {

	/** The vel. */
	public PVector vel;

	/** The acc. */
	public PVector acc;

	/** The has food. */
	public boolean hasFood = false;

	/** The last food pos. */
	public PVector lastFoodPos;

	/** The energy. */
	public float energy;

	/** The ready to lift. */
	public boolean readyToLift = false;

	/** The current brightness. */
	public float currentBrightness = 1f;

	/** The next offset brightness. */
	public float nextOffsetBrightness = 0.5f;

	/** The food sight. */
	public PVector foodSight;
	
	/** The leaf to carry. */
	public Leaf leaf;
	
	/** Where to bring back the food */
	public CellA cellToFeed;

	public HashMap<String, Object> getState() {
		HashMap<String, Object> state = super.getState();

		state.put("velX", vel.x);
		state.put("velY", vel.y);
		state.put("velZ", vel.z);
		
		state.put("accX", acc.x);
		state.put("accY", acc.y);
		state.put("accZ", acc.z);
		state.put("hasFood", hasFood);
		
		if (leaf != null)
			state.put("leaf", leaf.id);
		
		if (cellToFeed != null)
			state.put("cellToFeed", cellToFeed.id);

		state.put("energy", energy);
		state.put("readyToLift", readyToLift);
		state.put("currentBrightness", currentBrightness);
		state.put("nextOffsetBrightness", nextOffsetBrightness);

		if (lastFoodPos != null) {
			state.put("lastFoodPosX", lastFoodPos.x);
			state.put("lastFoodPosY", lastFoodPos.y);
		}
		if (foodSight != null) {
			state.put("foodSightX", foodSight.x);
			state.put("foodSightY", foodSight.y);
		}

		return state;
	}

	public void setState(HashMap<String, Object> state) {

		// Deal with vectors
		if (state.containsKey("velX") && state.containsKey("velY") && state.containsKey("velZ")) {
			this.vel = new PVector(Float.class.cast(state.get("velX")), Float.class.cast(state.get("velY")), Float.class.cast(state.get("velZ")));
			state.remove("velX");
			state.remove("velY");
			state.remove("velZ");
		}
		if (state.containsKey("accX") && state.containsKey("accY") && state.containsKey("accZ")) {
			this.acc = new PVector(Float.class.cast(state.get("accX")), Float.class.cast(state.get("accY")), Float.class.cast(state.get("accZ")));
			state.remove("accX");
			state.remove("accY");
			state.remove("accZ");
		}
		if (state.containsKey("foodSightX") && state.containsKey("foodSightY")) {
			this.foodSight = new PVector(Float.class.cast(state.get("foodSightX")), Float.class.cast(state
					.get("foodSightY")));
			state.remove("foodSightX");
			state.remove("foodSightY");
		}
		
		if (state.containsKey("lastFoodPosX") && state.containsKey("lastFoodPosY")) {
			this.foodSight = new PVector(Float.class.cast(state.get("lastFoodPosX")), Float.class.cast(state
					.get("lastFoodPosY")));
			state.remove("lastFoodPosX");
			state.remove("lastFoodPosY");
		}
		
		state.remove("highLander");

		super.setState(state);

	}

	/**
	 * Instantiates a new creature.
	 *
	 * @param pos
	 *            the pos
	 */
	public Lightie(PVector pos) {
		super(pos);
		this.pos = pos;
		if( this.pos != null)
			this.pos.z = CellA.MIN_DEPTH;
		acc = new PVector(0, 0, 0);
		vel = new PVector((float) (-1 + Math.random() * 2), (float) (-1 + Math.random() * 2), (float)Math.random());

		lastUpdate = System.currentTimeMillis();
		energy = 1f;
	}

	private void bringFoodBack(){
		//Bring the food back to one of the root cells
		if( cellToFeed == null || cellToFeed.level < 1.0f) //make sure the cell still exists
			cellToFeed = CellA.getRandomCell().getRootCell();
		
		if (pos.dist(cellToFeed.pos) < 5) {

			Mimodek.genetics.addFood();
			hasFood = false;
			seek(lastFoodPos);
			lastFoodPos = null;
		} else {
			seek(cellToFeed.pos);
			// drop some scent
			Mimodek.scentMap.addScent(pos.x - 5, pos.y - 5, 10, 10, 0.2f, lastFoodPos);
		}
	}
	
	private void removeLeaf(){
		if (leaf.edible) {
			// go move a b cell
			if (leaf.pos.dist(pos) < 5f) {
				// eat
				float amount = leaf.maturity >= 1f - energy ? 1f - energy : leaf.maturity;
				leaf.maturity -= amount;
				energy += amount;
				leaf = null;
			} else {
				seek(leaf.pos);
			}
		} else {
			PVector tPos = leaf.getCreatureTargetPosition(this);
			if (tPos == null) {
				leaf = null;
			} else if (!leaf.moving) {
				if (tPos.dist(pos) < 5f) {
					readyToLift = true;
				} else {
					seek(tPos);
				}
			} else {
				// move the cell out of the way
				if (this == leaf.carrierB) {
					leaf.startToLift(this);
				}
				if (tPos.dist(pos) < 10f) {
					readyToLift = false;
					if (!leaf.carrierA.readyToLift && !leaf.carrierB.readyToLift) {
						// can drop the cell now
						leaf.drop();
					}
				} else {
					seek(tPos);
				}
			}
		}
	}
	
	private void seekFood() {

		
		// no leaf being carried
		// look for food for the organism
		for (int i = 0; i < Mimodek.foods.size(); i++) {
			PVector f = Mimodek.foods.get(i);
			if (f != null && f.dist(pos) < 5) {
				// food has been found
				hasFood = true;
				lastFoodPos = f;
				Mimodek.foods.remove(f);
				Mimodek.foodAvg.sub(f);
				//Cell root = CellA.getRandomCell().getRootCell();
				//seek(root.pos);
				// directionAngle =
				// PApplet.atan2(root.pos.y-pos.y,root.pos.x-pos.x);
				return;
			}
		}
		
		// if we are very close to the food we saw before and it has
		// Disappeared, look for another piece
		if (foodSight != null) {
			if (pos.dist(foodSight) < 5f)
				foodSight = null;
			else
				seek(foodSight);
			return;
		}
		
		//Keep searching
		float directionAngle = PApplet.atan2(vel.y, vel.x);
		float cD = smellAround(directionAngle);
		if (cD == directionAngle) {
			float dist = 1000;
			PVector tmp = new PVector(pos.x, pos.y);
			for (int i = 0; i < Mimodek.foods.size(); i++) {
				PVector f = Mimodek.foods.get(i);
				float distT = pos.dist(f);
				if (distT < 50 && distT < dist) {
					dist = distT;
					tmp = f;
				}
			}
			if (dist < 1000) {
				foodSight = tmp;
				seek(tmp);
			} else if (Math.random() > 0.8) {
				seek(new PVector((float) (Math.random() * FacadeFactory.getFacade().width), (float) Math.random()
						* FacadeFactory.getFacade().height));

			}
			return;
		}
		//The steering direction has changed
		acc.add(new PVector(PApplet.cos(cD), PApplet.sin(cD)));
	}
	
	/**
	 * Update.
	 */
	public void update() {
		long timeSinceUpdate = System.currentTimeMillis() - lastUpdate;
		lastUpdate = System.currentTimeMillis();
		
		// deplete energy every 2 seconds (2000 millis)
		if ( !amIBusy() )
			energy -= (timeSinceUpdate / 2000f) * 0.03f;
		
		if (energy <= 0f) {// it's dead...
			return;
		}


		if (hasFood) {
			bringFoodBack();
		} else {
			// when energy is half full, seek for cell B to eat
			if (leaf == null && energy < 0.5f) {
				leaf = Leaf.getEatableCell();
			}

			if (leaf != null) { // I am carrying a leaf
				removeLeaf();
			}else{
				seekFood(); //look for food
			}
		}


		// Apply separation rule
		if (leaf != null && leaf.moving) {
			PVector mnt = maintainDistance();
			mnt.mult(1.0f); // weighted
			acc.add(mnt);
		} else if (!hasFood) {
			PVector sep = separate();
			sep.mult(5.0f); // weighted
			acc.add(sep);
		}

		if (PApplet.abs(nextOffsetBrightness - currentBrightness) < 0.005f) {
			nextOffsetBrightness = Configurator.getFloatSetting("CREATURE_ALPHA")
					- ((Mimodek.leavesCells.size() < Configurator.getIntegerSetting("CREATURE_DIM_THRESHOLD_INT") ? Configurator
							.getFloatSetting("CREATURE_ALPHA") : Configurator.getFloatSetting("CREATURE_ALPHA") / 2f)
							* (float) Math.random() * Configurator.getFloatSetting("CREATURE_ALPHA_VARIATION"));
		}
		currentBrightness += nextOffsetBrightness > currentBrightness ? 0.005f : -0.005f;

		// Update velocity
		vel.add(acc);
		// Limit speed
		limitSpeed();
		
		if (!FacadeFactory.getFacade().isInTheScreen(PVector.add(pos, acc),
				Configurator.getFloatSetting("CREATURE_SIZE"))) {
			if (leaf != null && leaf.moving) {
				leaf.drop();
			}
			seek(new PVector((float) (Math.random() * FacadeFactory.getFacade().width), (float) Math.random()
					* FacadeFactory.getFacade().height));
		}
		pos.add(vel);
		
		// Reset acceleration to 0 each cycle
		acc.mult(0);
	}
	
	public boolean amIBusy(){
		return hasFood || leaf != null;
	}

	
	
	public static Lightie getIdleLightie(){
		for(Lightie lightie : Mimodek.lighties){
			if( !lightie.amIBusy() )
				return lightie;
		}
		if( Configurator.getIntegerSetting("CREATURE_MAX_COUNT_INT") > Mimodek.lighties.size() )
			return spawn();
		return null;
	}
	
	
	protected void limitSpeed(){
		if (leaf != null && this == leaf.carrierB) {
			vel.limit(Configurator.getFloatSetting("CREATURE_MAXSPEED") * PApplet.min(1f, leaf.carrierA.energy));
		} else {
			vel.limit(Configurator.getFloatSetting("CREATURE_MAXSPEED") * PApplet.min(1f, energy));
		}
	}

	// compare position with other creatures and steer away if too close
	/**
	 * Separate.
	 *
	 * @return the p vector
	 */
	PVector separate() {
		float desiredseparation = Configurator.getFloatSetting("CREATURE_DISTANCE_BETWEEN");
		PVector sum = new PVector(0, 0, 0);
		int count = 0;
		// For every boid in the system, check if it's too close
		for (Lightie other : Mimodek.lighties) {
			float d = pos.dist(other.pos);
			// If the distance is greater than 0 and less than an arbitrary
			// amount (0 when you are yourself)
			if ((d > 0) && (d < desiredseparation)) {
				// Calculate vector pointing away from neighbor
				PVector diff = PVector.sub(pos, other.pos);
				diff.normalize();
				diff.div(d); // Weight by distance
				sum.add(diff);
				count++; // Keep track of how many
			}
		}

		// Average -- divide by how many
		if (count > 0) {
			sum.div(count);
		}
		return sum;
	}

	// maintain distance when moving cell b
	/**
	 * Maintain distance.
	 *
	 * @return the p vector
	 */
	PVector maintainDistance() {
		PVector constraint = new PVector(0, 0);
		if ((this == leaf.carrierA && PApplet.abs(pos.dist(leaf.carrierB.pos) - leaf.getSize()) > 5f)
				|| (this == leaf.carrierB && PApplet.abs(pos.dist(leaf.carrierA.pos) - leaf.getSize()) > 5f)) {
			float a = 0;
			if (this == leaf.carrierA) {
				if (pos.dist(leaf.carrierB.pos) - leaf.getSize() > 0) {
					a = PApplet.atan2(leaf.carrierB.pos.y - pos.y, leaf.carrierB.pos.x - pos.x);
				} else {
					a = PApplet.atan2(pos.y - leaf.carrierB.pos.y, pos.x - leaf.carrierB.pos.x);
				}
			} else {
				if (pos.dist(leaf.carrierA.pos) - leaf.getSize() > 0) {
					a = PApplet.atan2(leaf.carrierA.pos.y - pos.y, leaf.carrierA.pos.x - pos.x);
				} else {
					a = PApplet.atan2(pos.y - leaf.carrierA.pos.y, pos.x - leaf.carrierA.pos.x);
				}
			}
			constraint = new PVector(PApplet.cos(a), PApplet.sin(a));
		}
		return constraint;
	}

	// goes toward a point
	/**
	 * Seek.
	 *
	 * @param target
	 *            the target
	 */
	void seek(PVector target) {
		acc.add(steer(target, false));
	}

	// A method that calculates a steering vector towards a target
	// Takes a second argument, if true, it slows down as it approaches the
	// target
	/**
	 * Steer.
	 *
	 * @param target
	 *            the target
	 * @param slowdown
	 *            the slowdown
	 * @return the p vector
	 */
	PVector steer(PVector target, boolean slowdown) {
		PVector steer; // The steering vector
		PVector desired = PVector.sub(target, pos); // A vector pointing from
		// the location to the
		// target
		float d = desired.mag(); // Distance from the target is the magnitude of
		// the vector
		// If the distance is greater than 0, calc steering (otherwise return
		// zero vector)
		if (d > 0) {
			// Normalize desired
			desired.normalize();
			// Two options for desired vector magnitude (1 -- based on distance,
			// 2 -- maxspeed)
			if ((slowdown) && (d < 100.0))
				desired.mult(Configurator.getFloatSetting("CREATURE_MAXSPEED") * PApplet.min(1f, energy) * (d / 100.0f)); // This
			// damping
			// is
			// somewhat
			// arbitrary
			else
				desired.mult(Configurator.getFloatSetting("CREATURE_MAXSPEED") * PApplet.min(1f, energy));
			// Steering = Desired minus Velocity
			steer = PVector.sub(desired, vel);
			steer.limit(Configurator.getFloatSetting("CREATURE_STEER_FORCE")); // Limit
			// to
			// maximum
			// steering
			// force
		} else {
			steer = new PVector(0, 0, 0);
		}
		return steer;
	}

	/**
	 * Smell around.
	 *
	 * @param direction
	 *            the direction
	 * @return the float
	 */
	float smellAround(float direction) {

		PVector p = Mimodek.scentMap.getSmellInRect(pos.x - 5, pos.y - 5, 10, 10);

		if (p != null && p.dist(pos) < 5) {
			return direction;
		}

		if (p != null && (p.x != 0 || p.y != 0)) {
			return PApplet.atan2(p.y - pos.y, p.x - pos.x);
		}
		return direction;
	}

	/**
	 * Creates the creature.
	 *
	 * @param hL
	 *            the h l
	 * @return the creature
	 */
	public static Lightie spawn() {
		
		Cell root = CellA.getRandomCell().getRootCell();
		
		PVector pos = new PVector(root.pos.x + (-25.0f + (float) Math.random() * 50f), root.pos.y
				+ (-25.0f + (float) Math.random() * 50f));
		
		Lightie newLightie = new Lightie(pos);
		
		Mimodek.lighties.add(newLightie);
		return newLightie;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MimodekV2.Cell#radius()
	 */
	@Override
	public float radius() {
		return Configurator.getFloatSetting("CREATURE_SIZE");
	}
	
	public boolean isDead(){
		return leaf == null && energy <= 0f;
	}

}
