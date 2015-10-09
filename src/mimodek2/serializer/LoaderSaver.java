package mimodek2.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import mimodek2.Mimodek;
import mimodek2.bio.*;

public class LoaderSaver implements FilenameFilter {
	
	private static final String SAVE_PATH = "saved_states/";

	public static void loadFromFile(String loadFrom){
		loadFromFile(loadFrom, true);
	}
	
	public static boolean loadFromFile(){
		File lastStateFile;
		//Get a list of all the *.mmk files
		File dir = new File(Mimodek.getSketchPath(SAVE_PATH));     
		File[] files = dir.listFiles(new LoaderSaver());
		
		if( files.length < 1)
			return false;
		
		//Find the most recent file
		if( files.length == 1){
			lastStateFile = files[0];
		}else{
			lastStateFile = files[0];
			for(int i = 1; i < files.length; i++){
				if( lastStateFile.lastModified() < files[i].lastModified())
					lastStateFile = files[i];
			}
		}
		
		
		
		loadFromFile(SAVE_PATH+lastStateFile.getName(), true);
		return true;
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
			
			HashMap<Long, Cell> cells = new HashMap<Long, Cell>( loadedState.cells_A.size() + loadedState.cells_Leaves.size() + loadedState.creatures.size()  );
			
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
				Mimodek.allCells.add(cellA);
			}
			
			Leaf leaf;
			for( HashMap<String, Object> leavesState : loadedState.cells_Leaves ){
				leaf = new Leaf(null, 0);
				leaf.setState(leavesState);
				cells.put(leaf.id, leaf);
				Cell.nextId = Math.max(leaf.id, Cell.nextId);
				Mimodek.leavesCells.add(leaf);
				Mimodek.allCells.add(leaf);
			/*	
				if( leaf.id == 154 ){
					System.out.println("?");
				}
				*/
			}
			
			Lightie creature;
			boolean isHighLightie = false;
			for( HashMap<String, Object> creatureState : loadedState.creatures ){
				isHighLightie = creatureState.containsKey("highlander") && Boolean.class.cast( creatureState.remove("highlander") );
				creature = isHighLightie ? new HighLightie(null) : new Lightie(null);
				creature.setState(creatureState);
				cells.put(creature.id, creature);
				Cell.nextId = Math.max(creature.id, Cell.nextId);
				Mimodek.lighties.add(creature);
			}
			
			Cell.nextId++;
			
		
			//Second pass links the object instances
			for( HashMap<String, Object> cellAState : loadedState.cells_A ){
				cells.get( (Long)cellAState.get("id") ).link(cells, cellAState);
			}
			
			for( HashMap<String, Object> leavesState : loadedState.cells_Leaves ){
				cells.get( (Long)leavesState.get("id") ).link(cells, leavesState);
				if( cells.get( (Long)leavesState.get("id") ).anchor == null ){
					Mimodek.leavesCells.remove( cells.get( (Long)leavesState.get("id") ));
					Mimodek.allCells.remove( cells.get( (Long)leavesState.get("id") ));
					cells.remove( (Long)leavesState.get("id") );
				}
			}
			
			for( HashMap<String, Object> creatureState : loadedState.creatures ){
				cells.get( (Long)creatureState.get("id") ).link(cells, creatureState);
			}
			
			//Register growing cells
			for( long cellId : loadedState.growingCells){
				Mimodek.growingCells.add( cells.get(cellId) );
			}
			
			//Register cells to fossilize
			for( long cellId : loadedState.cellsToFossilize){
				Mimodek.cellsToFossilize.add( (CellA)cells.get(cellId) );
			}
			
			//Register leaves to be picked up
			for( long cellId : loadedState.leavesToBePickedUp){
				if( cells.get(cellId) == null)
					continue;
				if( cells.get( cellId ).anchor == null )
					continue;

				Leaf.toBePickedUp.add( (Leaf)cells.get(cellId) );
			}
			
			//Load genetics data
			Mimodek.genetics.setState( loadedState.lSystem );
			
			Mimodek.loadBackgroundImage(SAVE_PATH+loadedState.timestamp+".png");
			
			
			
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
		
		Mimodek.restartUpdateThread();
		
		System.gc();
	}
	
	public static void saveToFile(){
		saveToFile(null, true);
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
		
		
		
		State state = new State(Mimodek.aCells, Mimodek.cellsToFossilize, Mimodek.leavesCells, Leaf.toBePickedUp, Mimodek.lighties, Mimodek.growingCells, Mimodek.genetics);
		Mimodek.backgroundBuffer.save(SAVE_PATH+state.timestamp+".png");
		System.out.println("Saved state:\n"+state);
		
		saveTo = saveTo == null ? SAVE_PATH+state.timestamp+".mmk" : saveTo;
			
		
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

	public boolean accept(File dir, String fileName) {
		if (fileName.endsWith(".mmk")) {
			            return true;
		}
		return false;
	}
}
