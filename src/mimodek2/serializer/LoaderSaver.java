package mimodek2.serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import mimodek2.Mimodek;
import mimodek2.bio.*;

public class LoaderSaver {

	public static void loadFromFile(String loadFrom){
		loadFromFile(loadFrom, true);
	}
	
	public static void loadFromFile(String loadFrom, Boolean scheduleOnly){
		if(scheduleOnly){
			System.out.println(System.currentTimeMillis()+": scheduling state loading");
			Object[] args = new Object[]{loadFrom, false};
			
			try {
				Method method = LoaderSaver.class.getMethod("loadFromFile", new Class[]{String.class, Boolean.class});
				Mimodek.invokeAfterRender(method , args);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;
		}
		
		System.out.println(System.currentTimeMillis()+": loading state");
		
		
		
		try {
			FileInputStream fis = new FileInputStream(loadFrom);
			ObjectInputStream ois = new ObjectInputStream(fis);
			State loadedState = (State)ois.readObject();
			ois.close();
			fis.close();
			
			System.out.println("Loaded state:\n"+loadedState);
			
			HashMap<Long, Cell> cells = new HashMap<Long, Cell>( loadedState.cells_A.size() + loadedState.cells_B.size() + loadedState.creatures.size()  );
			
			//Clear current state
			Mimodek.reset();
			
			//First pass creates the objects instances
			CellA cellA;
			for( HashMap<String, Object> cellAState : loadedState.cells_A ){
				cellA = new CellA(null, 0);
				cellA.setState(cellAState);
				cells.put(cellA.id, cellA);
				Cell.nextId = Math.max(cellA.id, Cell.nextId);
				Mimodek.aCells.add(cellA);
				Mimodek.theCells.add(cellA);
			}
			
			CellB cellB;
			for( HashMap<String, Object> cellBState : loadedState.cells_B ){
				cellB = new CellB(null, 0);
				cellB.setState(cellBState);
				cells.put(cellB.id, cellB);
				Cell.nextId = Math.max(cellB.id, Cell.nextId);
				Mimodek.bCells.add(cellB);
				Mimodek.theCells.add(cellB);
			}
			
			Creature creature;
			for( HashMap<String, Object> creatureState : loadedState.creatures ){
				creature = new Creature(null, false);
				creature.setState(creatureState);
				cells.put(creature.id, creature);
				Cell.nextId = Math.max(creature.id, Cell.nextId);
				Mimodek.creatures.add(creature);
			}
			
			Cell.nextId++;
			
		
			//Second pass links the object instances
			for( HashMap<String, Object> cellAState : loadedState.cells_A ){
				cells.get( (Long)cellAState.get("id") ).link(cells, cellAState);
			}
			for( HashMap<String, Object> cellBState : loadedState.cells_B ){
				cells.get( (Long)cellBState.get("id") ).link(cells, cellBState);
			}
			for( HashMap<String, Object> creatureState : loadedState.creatures ){
				cells.get( (Long)creatureState.get("id") ).link(cells, creatureState);
			}
			
			//Register growing cells
			for( long cellId : loadedState.growingCells){
				Mimodek.growingCells.add( cells.get(cellId) );
			}
			
			//Load genetics data
			Mimodek.genetics.setState( loadedState.lSystem );
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.gc();
	}
	
	public static void saveToFile(String saveTo){
		saveToFile(saveTo, true);
	}
	
	public static void saveToFile(String saveTo, Boolean scheduleOnly){
		if(scheduleOnly){
			Object[] args = new Object[]{saveTo, false};
			
			try {
				Method method = LoaderSaver.class.getMethod("saveToFile", new Class[]{String.class, Boolean.class});
				Mimodek.invokeAfterRender(method , args);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;
		}
		
		
		State state = new State(Mimodek.aCells, Mimodek.bCells, Mimodek.creatures, Mimodek.growingCells, Mimodek.genetics);
		
		System.out.println("Saved state:\n"+state);
		
		try {
			FileOutputStream fos = new FileOutputStream(saveTo);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(state);
			oos.flush();
			oos.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.gc();
		
	}
}
