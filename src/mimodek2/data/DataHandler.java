package mimodek2.data;

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
import mimodek2.serializer.LoaderSaver;
import processing.core.PApplet;

// TODO: Auto-generated Javadoc
/**
 * The Class DataHandler.
 */
public class DataHandler implements Runnable/*, WPMessageListener */{
	
	private static final String COMMANDS_URL= "http://krem.io/other/mimodek/consumeCommands.php?key=youplaboum";
	private static final String REGISTER_IP_URL = "http://krem.io/other/mimodek/registerIP.php?ip=";
	
	/** The runner. */
	Thread runner = null;
	
	/** The run. */
	boolean run = false;
	
	/** The location. */
	MimodekLocation location;
	
	/** The data interpolators. */
	public ArrayList<DataInterpolator> dataInterpolators = new ArrayList<DataInterpolator>();
	
	/** The wunderground. */
	protected WeatherUndergroundClient wunderground;
	
	/** The mapping. */
	public HashMap<String,String> mapping = new HashMap<String,String>();

	private PApplet app;
	
	/** The xml receiver. */
	//private XMLReceiver xmlReceiver;
	
	/** The instance counter. */
	public static int instanceCounter = 0;
	
	public static long timeToNextSave = 0;
	
	/** The instance. */
	protected static DataHandler instance;
	
	/**
	 * Test data source.
	 *
	 * @param app the app
	 * @param location the location
	 * @return true, if successful
	 * @throws Exception 
	 */
	public static boolean testDataSource(PApplet app , MimodekLocation location) throws Exception{

		if(Configurator.getBooleanSetting("FAKE_DATA_FLAG")){
			System.out.println("Fake data!");
			return true;
		}
		
		WeatherUndergroundClient wunderground = new WeatherUndergroundClient(app);
		if(wunderground.checkForStation(location)){
			Verbose.debug("A weather station for the location has been found.");
			HashMap<String,String> testMap = new HashMap<String, String>();
			testMap.put("temp_c", "DATA_TEMPERATURE");
			testMap.put("relative_humidity", "DATA_HUMIDITY");
			if(wunderground.readLatestObservation(location, testMap)){
				Verbose.debug("The weather station is working properly.");
				return true;
			}else{
				Verbose.overRule("A problem occured reading from the weather station. Check net connection.");
				return true;
			}
		}else{
			Verbose.overRule("Couldn't find a weather station for location:"+location+". Try providing more information about the location.");
			return false;
		}
		
	}
	
	/**
	 * Gets the single instance of DataHandler.
	 *
	 * @param location the location
	 * @param app the app
	 * @return single instance of DataHandler
	 * @throws Exception 
	 */
	public static DataHandler getInstance(MimodekLocation location, PApplet app){
		if(instance == null)
			instance = new DataHandler(location, app);
		return instance;
	}
	
	/**
	 * Instantiates a new data handler.
	 *
	 * @param location the location
	 * @param app the app
	 * @throws Exception 
	 */
	private DataHandler(MimodekLocation location, PApplet app) {
		
		this.app = app;
		this.location = location;
		
		if (!Configurator.getBooleanSetting("FAKE_DATA_FLAG")) {

			// Weather underground data source
			wunderground = new WeatherUndergroundClient(app);
			
		}
		
		runner = new Thread(this);
		instanceCounter++;
		updateTimeToSave();
		Verbose.debug("I'm instance #"+instanceCounter);
	}
	
	private void updateTimeToSave(){
		timeToNextSave = System.currentTimeMillis() + Configurator.getIntegerSetting("AUTO_SAVE_DELAY_LONG");
	}
	
	public void useRealData(PApplet app){
		if( wunderground == null ){
			wunderground = new WeatherUndergroundClient(app);
		}
		Configurator.setSetting("FAKE_DATA_FLAG", false);		
	}
	
	/**
	 * Map.
	 *
	 * @param key the key
	 * @param data the data
	 */
	public void map(String key, String data){
		mapping.put(key, data);
	}
	
	/**
	 * Adds the interpolator.
	 *
	 * @param dataInterpolator the data interpolator
	 */
	public void addInterpolator(DataInterpolator dataInterpolator) {
		dataInterpolators.add(dataInterpolator);
	}
	
	/**
	 * Start.
	 */
	public void start(){
		if(run || runner == null){
			Verbose.debug("Data handling thread already started or not set.");
			return;
		}
		run = true;
		runner.start();
		Verbose.debug("Data handling thread started.");
	}
	
	/**
	 * Stop.
	 */
	public void stop(){
		run = false;
	}
	
	private void registerIp(){
		String httpQuery = REGISTER_IP_URL+Mimodek.myLocalIP;
		try{
			app.loadStrings(httpQuery);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void checkForRemoteCommands(){
		String httpQuery = COMMANDS_URL;
		
		try{
			for(String  command : app.loadStrings(httpQuery)){
				if( command.equals("showConsole") ){
					Mimodek.jsConsole.openConsole();
					continue;
				}else if( command.equals("hideConsole") ){
					Mimodek.jsConsole.closeConsole();
					continue;	
				}else{
					Mimodek.jsConsole.runCommand(command);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean dataSourceOnline = false;
		while ( run ) {
			
			if (Configurator.getBooleanSetting("AUTO_SAVE_STATE_FLAG") && System.currentTimeMillis() > timeToNextSave ){
					LoaderSaver.saveToFile();
					updateTimeToSave();
			}
			
			// Test that we can get a weather station for this location
			// NOTE: this test has a side effect of setting the starting values for
			// the weather variable, neat!
			if (Configurator.getBooleanSetting("FAKE_DATA_FLAG")) {
				dataSourceOnline = false;
				for (int i = 0; i < dataInterpolators.size(); i++)
					dataInterpolators.get(i).update();
			} else if (wunderground != null) {
				// Query fresh data
				try {
					if (wunderground.readLatestObservation(location, mapping)) {
						for (int i = 0; i < dataInterpolators.size(); i++)
							dataInterpolators.get(i).update();
					}
					dataSourceOnline = true;
				} catch (Exception ex) {
					// ex.printStackTrace();
					dataSourceOnline = false;
				}

			}

			if (dataSourceOnline) {
				registerIp();
				checkForRemoteCommands();
			}
			
			dataSourceOnline = false;
			
			try {		
				Thread.sleep((long)(Configurator.getFloatSetting("DATA_REFRESH_RATE") * 60 * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * TODO: What follows should most likely be removed.  
	 */
	
	/**
	 * @param messages
	 */
	/*
	 * This deals with the data returned by the wordpress plugin (pollution,...), not the weather data
	 */
	/* (non-Javadoc)
	 * @see p5wp.WPMessageListener#onResponse(java.util.HashMap)
	 */
	public void onResponse(HashMap<String,String> messages) {
		try{
		/*
		 * <item name='NO2'>Bueno</item> <item name='CO'>Bueno</item> <item
		 * name='SO2'>Bueno</item> <item name='particles'>Bueno</item> <item
		 * name='03'>Admisible</item> <item name='temperature'>17.4</item> <item
		 * name='rain'>0.0</item>
		 */
		//Verbose.overRule("Last data received at : " + Verbose.now());
			PollutionLevelsEnum[] pollutionData = new PollutionLevelsEnum[5];
			int c = 0;
	
			String[] keys = messages.keySet().toArray(new String[0]);
			for (int i = 0; i < keys.length; i++) {
				try {
					if (keys[i].equalsIgnoreCase("NO2")) {
						pollutionData[c++] = PollutionLevelsEnum
								.getPollutionLevelForWord(messages
										.get(keys[i]));
					} else if (keys[i].equalsIgnoreCase("CO")) {
						pollutionData[c++] = PollutionLevelsEnum
								.getPollutionLevelForWord(messages
										.get(keys[i]));	
					} else if (keys[i].equalsIgnoreCase("SO2")) {
						pollutionData[c++] = PollutionLevelsEnum
								.getPollutionLevelForWord(messages
										.get(keys[i]));
					} else if (keys[i].equalsIgnoreCase("particles")) {
						pollutionData[c++] = PollutionLevelsEnum
								.getPollutionLevelForWord(messages
										.get(keys[i]));
					} else if (keys[i].equalsIgnoreCase("03")) {
						pollutionData[c++] = PollutionLevelsEnum
								.getPollutionLevelForWord(messages
										.get(keys[i]));
					}
					Verbose.debug(keys[i] + ": " + messages.get(keys[i]));
				} catch (NotAPollutionLevelException e) {
					e.printStackTrace();
				}
				
			}
			if (c == pollutionData.length) {
				//CellB.usePollutionData = true;
				Configurator.setSetting("DATA_POLLUTION", (float)PollutionLevelsEnum.calculatePollutionScore(pollutionData).getScore());
				Verbose.debug("New pollution level: "+PollutionLevelsEnum.getPollutionLevelForScore(Configurator.getFloatSetting("DATA_POLLUTION")));
				//Verbose.overRule("Pollution level set.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
