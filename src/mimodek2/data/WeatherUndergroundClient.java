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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mimodek2.*;
import processing.core.PApplet;
import processing.data.JSONObject;


// TODO: Auto-generated Javadoc
/**
 * The Class WeatherUndergroundClient.
 */
public class WeatherUndergroundClient {
	
	/** The Constant GEO_LOOKUP_URL. */
	public final static String GEO_LOOKUP_URL = "http://api.wunderground.com/api/";
	
	/** The Constant CURRENT_OBSERVATION_URL. */
	//public final static String CURRENT_OBSERVATION_URL = "http://api.wunderground.com/weatherstation/WXCurrentObXML.asp";
	
	/** The Constant CITY_COUNTRY. */
	//private final static int CITY_COUNTRY = 0;
	
	/** The Constant LAT_LON. */
	//private final static int LAT_LON = 2;
	
	/** The app. */
	private PApplet app;
	
	/** The use station. */
	//private int useStation = 0;
	
	/** The station ids. */
	//private ArrayList<String> stationIds = new ArrayList<String>();
	
	
	/**
	 * Instantiates a new weather underground client.
	 *
	 * @param app the app
	 */
	public WeatherUndergroundClient(PApplet app){
		this.app = app;
	}
	
	/**
	 * Test if there is an available weather station at the location.
	 * 
	 * @param location The location to search for
	 * @return true if a station was found, false otherwise
	 */
	public boolean checkForStation(MimodekLocation location){
		
		String httpQuery = GEO_LOOKUP_URL;
		httpQuery += Configurator.getStringSetting("WU_API_KEY_STR");
		httpQuery += "/conditions/q/" + location.country + "/" + location.city;
		httpQuery += ".json";
		
		try{
			JSONObject jsonObject = app.loadJSONObject( httpQuery );

			return jsonObject.hasKey("current_observation");
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

	}
	
	/**
	 * Retrieve the current observation for the registered station and save the data in the configurator.
	 * The hash map maps weather data as they are named in the  xml/json files of weather underground (key), to data as they are named in mimodek(value)
	 * e.g. : "temp_c","DATA_TEMPERATURE"
	 *
	 * @param location The location to search for
	 * @param wuToMimodek Maps Weather Underground observation labels to Mimodek data labels
	 * @return true, if successful
	 * @throws Exception 
	 */
	public boolean readLatestObservation(MimodekLocation location,HashMap<String,String> wuToMimodek) throws Exception{
		
		try{
			JSONObject jsonObject = app.loadJSONObject(GEO_LOOKUP_URL
					+ Configurator.getStringSetting("WU_API_KEY_STR")
					+ "/conditions/q/" + location.country + "/" + location.city
					+ ".json");
			
			//No station found
			if( !jsonObject.hasKey("current_observation") )
				return false;
			
			JSONObject currentObservation = jsonObject.getJSONObject("current_observation");
			
			
			Verbose.overRule("Reading weather data from station:"+currentObservation.getString("station_id"));
			Verbose.debug( currentObservation.getString("observation_time") );
			
			Iterator<Entry<String, String>> wuIt = wuToMimodek.entrySet().iterator();
			float observation;
			while(wuIt.hasNext()){
				Entry<String, String> wu = wuIt.next();
				//Remove any unit character and convert to float
				try{
					observation = currentObservation.getFloat( wu.getKey() );
				}catch(Exception e){
					observation = Float.parseFloat( currentObservation.getString( wu.getKey() ).replaceAll("[^0-9.]", "") );
				}
				Configurator.setSetting(wu.getValue(),  observation ); // Float.parseFloat( xml.getChild(wu.getKey()).getContent())  );
				Verbose.debug(wu.getValue()+","+Configurator.getFloatSetting(wu.getValue()));
			}
			return true;
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		/*
		if(stationIds.size()<1){
			if(!findStation(location))
				return false;
		}
		try{
			boolean stationOk = false;
			XML xml = app.loadXML( CURRENT_OBSERVATION_URL+"?ID="+stationIds.get(useStation));
			while(!stationOk){
				//check the validity of the response
				if(!xml.getChild("station_id").getContent().equalsIgnoreCase(stationIds.get(useStation))){
					useStation++;
					if(useStation>=stationIds.size()){
						useStation = 0;
						return false;
					}
				}else{
					stationOk = true;
				}
			}
			Verbose.overRule("Reading weather data from station:"+stationIds.get(useStation));
			Verbose.debug(xml.getChild("observation_time").getContent());
			Iterator<Entry<String, String>> wuIt = wuToMimodek.entrySet().iterator();
			while(wuIt.hasNext()){
				Entry<String, String> wu = wuIt.next();
				Configurator.setSetting(wu.getValue(),Float.parseFloat(xml.getChild(wu.getKey()).getContent()));
				Verbose.debug(wu.getValue()+","+Configurator.getFloatSetting(wu.getValue()));
			}
			return true;
		}catch(Exception e){
			//useStation
			//e.printStackTrace();
			//try to recover by switching to next station
			Verbose.overRule("An error happened reading weather data from station "+stationIds.get(useStation));
			if(useStation<stationIds.size()-2){
				useStation++;
			}else{
				if(!findStationByCityAndCountry(location)){
					Verbose.overRule("No weather station for "+location+" has been found. Weather data won't be updated.");
					return false;
				}
			}
			Verbose.overRule("Weather data will now be read from station: "+stationIds.get(useStation)+".");
			return readLatestObservation(location, wuToMimodek);
		}
		*/
	}
	
	
	/*
	 * Find a station close the location
	 */	
	/**
	 * Find station.
	 *
	 * @param location the location
	 * @return true, if successful
	 * @throws Exception 
	 */
	/*
	public boolean findStation(MimodekLocation location) throws Exception{
		int searchBy = LAT_LON;
		if(location.latitude == null  || location.longitude== null){
			if(location.city != null && location.country != null){
				searchBy = CITY_COUNTRY;
			}else{
				return false;
			}
		}
		boolean result = false;
		switch(searchBy){
			case CITY_COUNTRY:
				result = findStationByCityAndCountry(location);
				break;
			case LAT_LON:
				result = findStationByCoordinates(location);
				break;
		}
		return result;
	}
	*/
	
	/**
	 * Find station by coordinates.
	 *
	 * @param location the location
	 * @return true, if successful
	 */
	/*
	public boolean findStationByCoordinates(MimodekLocation location){
		try{
			XML xml = app.loadXML( GEO_LOOKUP_URL+Configurator.getStringSetting("WU_API_KEY")+"/geolookup/q/"+location.latitude+","+location.longitude );
			location.city = xml.getChild("city").getContent();
			location.country = xml.getChild("country").getContent();
			
			XML private_stations = xml.getChild("nearby_weather_stations/pws");
			if(private_stations.getChildCount()==0)
				throw new Exception("No station for this location, sorry");
			useStation = 0;
			for(int i=0;i<private_stations.getChildCount();i++){
				stationIds.add(private_stations.getChild(i).getChild("id").getContent());
			}
			//Verbose.overRule("Reading weather data from station:"+stationIds.get(useStation));
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	*/
	
	/**
	 * Find station by city and country.
	 *
	 * @param location the location
	 * @return true, if successful
	 * @throws Exception 
	 */
	/*
	public boolean findStationByCityAndCountry(MimodekLocation location) throws NoWeatherStationException{
		JSONArray nearbyWeatherStations;
		
		try{
			JSONObject jsonObject = app.loadJSONObject(GEO_LOOKUP_URL
					+ Configurator.getStringSetting("WU_API_KEY_STR")
					+ "/geolookup/q/" + location.country + "/" + location.city
					+ ".json");

			JSONObject locationJSON = jsonObject.getJSONObject("location");
			location.latitude = locationJSON.getFloat("lat");
			location.longitude = locationJSON.getFloat("lon");
			
			nearbyWeatherStations = locationJSON.getJSONObject("pws").getJSONArray("station");
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		if( nearbyWeatherStations.size() == 0 )
			throw new NoWeatherStationException("No weather station for this location, sorry");
		
		useStation = 0;
		
		for(int i = 0; i < nearbyWeatherStations.size(); i++){
			stationIds.add( nearbyWeatherStations.getJSONObject(i).getString("id") );
		}
		
		return true;
	
	}
	*/
	
	/*
	 * e.g. : "Madrid","Spain"
	 * TODO: It would be great if this returned false if there was a problem  finding a station
	 */
/*
	public boolean findStationByCityAndCountry(String city, String country){
		try{
			XMLElement xml = new XMLElement(app, GEO_LOOKUP_URL+"?query="+city+","+country);
			XMLElement private_station = xml.getChild("nearby_weather_stations/pws").getChild(0);
			stationId = private_station.getChild("id").getContent();
			Verbose.debug("Reading temperature data from station:"+stationId);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	*/
	

	
	
	
	
}
