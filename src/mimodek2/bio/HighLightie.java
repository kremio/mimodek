package mimodek2.bio;

import java.util.HashMap;

import mimodek2.Configurator;
import mimodek2.Mimodek;
import processing.core.PApplet;
import processing.core.PVector;

public class HighLightie extends Lightie {
	
	protected Lightie myTarget;

	public static HighLightie spawn() {
		Cell root = CellA.getRandomCell().getRootCell();
		
		PVector pos = new PVector(root.pos.x + (-25.0f + (float) Math.random() * 50f), root.pos.y
				+ (-25.0f + (float) Math.random() * 50f));
		
		HighLightie newLightie = new HighLightie(pos);
		
		Mimodek.lighties.add(newLightie);
		return newLightie;
	}
	
	public HashMap<String, Object> getState() {
		HashMap<String, Object> state = super.getState();
		state.put("highLander", true);
		return state;
	}
	
	public HighLightie(PVector pos) {
		super(pos);
	}
	
	/**
	 * Maintain energy level
	 */
	public void update(){
		energy = Math.max( 0.4f, energy);
		super.update();
		
		//Check the status of the target
		if( !amIHunting() ){
			//the target is gone
			myTarget = null;
		}
		
		hunt();
	}
	
	public boolean amIBusy(){
		return true; //always busy!
	}
	
	public boolean amIHunting(){
		return myTarget != null && myTarget.energy > 0;
	}
	
	protected void hunt(){
		//Do I have a valid target?
		if( !amIHunting() )
			return;
		
		//Am I busy doing something else?
		if( cellB != null && cellB.moving )
			return;
		
		seek( myTarget.pos );
		
		//If the target has been caught, take its energy away
		if( myTarget.pos.dist( pos ) < 10f){
			myTarget.energy -= 0.1f;
			energy = Math.min(1f, energy + 0.1f);
		}
	}
	
	protected void limitSpeed(){
		if (cellB != null && this == cellB.carrierB) {
			vel.limit(Configurator.getFloatSetting("CREATURE_MAXSPEED") * PApplet.min(1f, cellB.carrierA.energy));
		} else if( amIHunting() ){
			vel.limit(Configurator.getFloatSetting("CREATURE_MAXSPEED") );
		}else{
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
			if( amIHunting() && other.id == myTarget.id) //do not avoid the target!
				continue;
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
	
	
	/**
	 * Assign a target for the HighLightie to hunt.
	 * 
	 * @param theTarget
	 * @return true is the hunt was accepted, false if the HighLightie was already hunting
	 */
	public boolean huntTarget(Lightie theTarget){
		//Do I already have a viable target?
		if ( amIHunting() ) {
			//already hunting
			return false;
		}
		
		myTarget = theTarget;
		return true;
	}

}
