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
import mimodek2.*;
import mimodek2.serializer.StatefulObject;

// TODO: Auto-generated Javadoc
/**
 * The Class LSystem.
 */
public class LSystem implements StatefulObject{
	
	/** The app. */
	PApplet app;

	/** The time since last food. */
	long timeSinceLastFood = 0;
	
	//STATE DATA
	
	/** The command index. */
	public int commandIndex = 0;
	
	/** The command str. */
	public String commandStr = "ab";

	/** The create eatable counter. */
	public int createEatableCounter = 0;

	/**
	 * Instantiates a new l system.
	 *
	 * @param seed the seed
	 * @param app the app
	 */
	public LSystem(String seed, PApplet app) {
		commandStr = seed;
		this.app = app;
		timeSinceLastFood = System.currentTimeMillis();
	}

	/**
	 * Command a.
	 *
	 * @return 1 if a cell A was added, -1 if a cell A could not be added, 0 if nothing happened
	 */
	private int commandA() {
		float ratio = Mimodek.aCells.size() > 0 ? (float) Mimodek.leavesCells.size()
				/ (float) Mimodek.aCells.size() : 0f;
		
		if (ratio < 1.25f) {
			return 0;
		}
			
		// Try to create a new Cell A
		CellA nuC = CellA.addCellA();

		if (nuC != null) {
			Mimodek.growingCells.add(nuC);
			Mimodek.aCells.add(nuC);
			Mimodek.allCells.add(nuC);
			return 1;
		}

		// make one bCell edible
		for (Leaf cellB : Mimodek.leavesCells) {
			if (!cellB.edible && cellB.currentMaturity >= 1f && cellB.creatureA == null && cellB.creatureB == null) {
				cellB.makeEdible();
				break;
			}
		}
		return -1;
	}

	/**
	 * Command b.
	 */
	private void commandB() {
		Leaf nuC = Leaf.addCellB();
		
		if (nuC != null) {
			Mimodek.growingCells.add(nuC);
			Mimodek.leavesCells.add(nuC);
			Mimodek.allCells.add(nuC);
			createEatableCounter++;
			// TODO: arbitrary values
			if (createEatableCounter == 3) {
				nuC.makeEdible();
				createEatableCounter = 0;
			}
			return;
		}
		
		float ratio = Mimodek.aCells.size() > 0 ? (float) Mimodek.leavesCells.size() / (float) Mimodek.aCells.size() : 0f;
		if (ratio < 1.5f) {
			return;
		}
		
		// make one bCell edible
		for (Leaf cellB : Mimodek.leavesCells) {
			
			if (!cellB.edible && cellB.currentMaturity >= 1f
					&& cellB.creatureA == null && cellB.creatureB == null) {
				cellB.makeEdible();
				break;
			}
		}
	}

	/**
	 * Regenerate.
	 */
	private void regenerate() {
		commandIndex = 0;
		if (commandStr.length() >= 256) {
			commandStr = "ab";
			return;
		}

		StringBuilder nuCmd = new StringBuilder(); // More effective when constructing strings in loops
		for (int i = 0; i < commandStr.length(); i++) {
			char c = commandStr.charAt(i);
			switch (c) {
			case 'a':
				nuCmd.append(Math.random() >= 0.5 ? "b" : "ab");
				break;
			case 'b':
				nuCmd.append(Math.random() >= 0.5 ? "aa" : "ab");
				break;
			}
		}
		commandStr = nuCmd.toString();
		
	}

	/**
	 * Adds the food.
	 */
	public void addFood() {
		long elapsedTime = System.currentTimeMillis() - timeSinceLastFood;
		// if it has been more than 5 seconds, create a creature
		if (elapsedTime > 5 * 1000) {
			Lightie.spawn();
		}
		timeSinceLastFood = System.currentTimeMillis();
		
		//Further the growth of an immature cell
		if (Mimodek.growingCells.size() > 0) {
			Cell cell = Mimodek.growingCells.get((int) (float) Math.random()
					* (Mimodek.growingCells.size()));
			cell.maturity += 0.1;
			if (cell.maturity >= 1.0f) {
				cell.maturity = 1.0f;
				Mimodek.growingCells.remove(cell);
			}
			return;
		}
		
		//Otherwise generate a new cell
		
		//Regenerate the genetics string if its end had been reached.
		if (commandIndex >= commandStr.length()) {
			regenerate();
		}
		
		char c = commandStr.charAt(commandIndex);
		
		switch (c) {
		case 'a':
			int t = commandA();
			if (t == 1) {
				commandIndex++;
				break;
			} else if (t < 0) { //There should be something to eat now
				Lightie.goEatALeaf();
				break;
			}
		case 'b':
			commandB();
			commandIndex++;
			break;
		}

	}

	public HashMap<String, Object> getState() {
		HashMap<String, Object> state = new HashMap<String, Object>();
		state.put("commandIndex", commandIndex);
		state.put("commandStr", commandStr);
		state.put("createEatableCounter", createEatableCounter);
		return state;
	}

	public void setState(HashMap<String, Object> state) {
		Class<?> thisClass = this.getClass();
		Set<String> keys = state.keySet();
		Field theField;
		for(String key : keys){
			try{
				//If the field is found
				theField = thisClass.getField( key );
				
				//set its value
				theField.set(this, state.get(key) );
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}	
		}
	}
}
