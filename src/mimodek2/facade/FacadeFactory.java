package mimodek2.facade;

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

import mimodek2.Configurator;
import processing.core.PApplet;

// TODO: Auto-generated Javadoc
/**
 * A factory to configure which facades to use.
 * 
 * @author Jonsku
 */
public class FacadeFactory {
	
	/** The current facade. */
	protected static Facade currentFacade = null;
	
	/** The Constant PRADO_FACADE. */
	public static final int PRADO_FACADE = 1;
	
	/** The Constant FULL_WINDOW. */
	public static final int FULL_WINDOW = 2;

	/** The Constant FULL_WINDOW. */
	public static final int MINI_PAL = 3;
	
	/** The Constant FULL_WINDOW. */
	public static final int SCALED = 4;
	
	/**
	 * Configure a Media Lab Prado Media Facade.
	 *
	 * @param facadeType the facade type
	 * @param app Parent sketch
	 * @return the facade
	 */
	public static Facade createFacade(int facadeType, PApplet app) {
		switch (facadeType) {
		case PRADO_FACADE:
			return createPradoFacade(app);
		case FULL_WINDOW:
			return createProcessingFacade(app);
		case MINI_PAL:
			return createMiniPalFacade(app);
		case SCALED:
			return createScaledFacade(app);
		default:
			return createProcessingFacade(app);
		}
	}

	private static Facade createScaledFacade(PApplet app) {
		currentFacade = new ScaledFacade(app, Configurator.getIntegerSetting("SURFACE_WIDTH_INT"), Configurator.getIntegerSetting("SURFACE_HEIGHT_INT"));
		return currentFacade;
	}

	public static Facade createMiniPalFacade(PApplet app) {
		currentFacade = new MiniPalFacade(app);
		return currentFacade;
	}

	/**
	 * Configure a Media Lab Prado Media Facade.
	 *
	 * @param app Parent sketch
	 * @return the facade
	 */
	public static Facade createPradoFacade(PApplet app) {
		currentFacade = new MadridPradoFacade(app);
		return currentFacade;
	}

	/**
	 * Configure a Processing Facade, it is a rectangle of the same dimensions
	 * as the parent sketch.
	 *
	 * @param app Parent sketch
	 * @return the facade
	 */
	public static Facade createProcessingFacade(PApplet app) {
		currentFacade = new ProcessingFacade(app);
		return currentFacade;
	}

	/**
	 * Gets the facade.
	 *
	 * @return The configured facade.
	 */
	public static Facade getFacade() {
		return currentFacade;
	}

}
