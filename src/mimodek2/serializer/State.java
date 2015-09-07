package mimodek2.serializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import mimodek2.bio.*;

public class State implements Serializable {

	/**
	 * Class version for serialization 
	 */
	private static final long serialVersionUID = 1L;
	
	ArrayList< HashMap<String, Object> > cells_A;
	ArrayList< HashMap<String, Object> > cells_B;
	ArrayList< HashMap<String, Object> > creatures;
	
	public State(ArrayList<CellA> aCells, ArrayList<CellB> bCells, ArrayList<Creature> creatures){
		
		//Get the state of all cells of type A
		cells_A = new ArrayList< HashMap<String, Object> >(aCells.size());
		for(CellA aCell : aCells){
			cells_A.add( aCell.getState() );
		}
		
		//Get the state of all cells of type B
		cells_B = new ArrayList< HashMap<String, Object> >(bCells.size());
		for(CellB bCell : bCells){
			cells_B.add( bCell.getState() );
		}
		
		//Get the state of all creatures
		this.creatures = new ArrayList<HashMap<String, Object>>(creatures.size());
		for (Creature creature : creatures) {
			this.creatures.add(creature.getState());
		}
	}
	
}
