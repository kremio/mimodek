package mimodek2.serializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mimodek2.Configurator;
import mimodek2.bio.*;

public class State implements Serializable {

	/**
	 * Class version for serialization 
	 */
	private static final long serialVersionUID = 1L;
	
	ArrayList< HashMap<String, Object> > cells_A;
	ArrayList< HashMap<String, Object> > cells_Leaves;
	ArrayList< HashMap<String, Object> > creatures;
	HashMap<String, Object> lSystem;
	ArrayList< Long > growingCells;
	ArrayList< Long > cellsToFossilize;
	ArrayList< Long > leavesToBePickedUp;
	float humidity;
	float temperature;
	long timestamp;
	
	public State(ArrayList<CellA> aCells, ArrayList<CellA> cellsToFossilize, ArrayList<Leaf> bCells, HashSet<Leaf> leavesToBePickedUp, ArrayList<Lightie> creatures, ArrayList<Cell> growingCells, LSystem lSystem){
		
		//Get the state of all cells of type A
		cells_A = new ArrayList< HashMap<String, Object> >(aCells.size());
		for(CellA aCell : aCells){
			cells_A.add( aCell.getState() );
		}
		
		//Get the state of all cells of type B
		cells_Leaves = new ArrayList< HashMap<String, Object> >(bCells.size());
		for(Leaf bCell : bCells){
			cells_Leaves.add( bCell.getState() );
		}
		
		//Get the state of all creatures
		this.creatures = new ArrayList<HashMap<String, Object>>(creatures.size());
		for (Lightie creature : creatures) {
			this.creatures.add(creature.getState());
		}
		
		this.growingCells = new ArrayList< Long >( growingCells.size() );
		for(Cell cell : growingCells){
			this.growingCells.add( cell.id );
		}
		
		this.cellsToFossilize = new ArrayList< Long >( cellsToFossilize.size() );
		for(Cell cell : growingCells){
			this.cellsToFossilize.add( cell.id );
		}
		
		this.leavesToBePickedUp = new ArrayList< Long >( leavesToBePickedUp.size() );
		for(Leaf leaf : leavesToBePickedUp){
			this.leavesToBePickedUp.add( leaf.id );
		}
		
		
		this.lSystem = lSystem.getState();
		
		humidity = Configurator.getFloatSetting("DATA_HUMIDITY");
		temperature = Configurator.getFloatSetting("DATA_TEMPERATURE");
		timestamp = System.currentTimeMillis();
	}
	
	public String toString(){
		String stats = "A cells: "+cells_A.size()+"\n";
		stats += "B cells: "+cells_Leaves.size()+"\n";
		stats += "Creatures: "+creatures.size()+"\n";
		
		int droppedCells = 0;
		for( HashMap<String, Object> bCellState : cells_Leaves ){
			if( bCellState.containsKey("dropX") && bCellState.containsKey("dropY") ){
				droppedCells++;
			}
		}
		
		HashSet<Long> carriedCellsList = new HashSet<Long>();
		
		for( HashMap<String, Object> creatureState : creatures ){
			if( creatureState.containsKey("cellB") ){
				carriedCellsList.add( (Long)creatureState.get("cellB") );
			}
		}
		
		stats += "Carried cellB: "+ carriedCellsList.size() +"\n";
		stats += "Dropped cellB: "+ droppedCells+"\n";
		
		return stats;
	}
	
}
