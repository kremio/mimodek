
package mimodek2;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import mimodek2.tracking.*;
import mimodek2.data.*;
import mimodek2.bio.*;
import mimodek2.facade.*;
import mimodek2.graphics.*;
import processing.core.*;

public class Mimodek implements TrackingListener {
	
	/** The number of vertices making up the geometry of type A cells */
	public static int CELLA_VERTEX_NUMBER = 20;
	
	private static PApplet app;

	private DataHandler dataHandler;

	//private boolean reset;

	private boolean autoFood;

	private TUIOClient tuioClient;
	
	/** The cells. */
	public static ArrayList<Cell> theCells = new ArrayList<Cell>();
	
	/** The a cells. */
	public static ArrayList<CellA> aCells = new ArrayList<CellA>();
	
	/** The b cells. */
	public static ArrayList<CellB> bCells = new ArrayList<CellB>();
	
	/** The growing cells. */
	public static ArrayList<Cell> growingCells = new ArrayList<Cell>();

	/** The foods. */
	public static ArrayList<Food> foods = new ArrayList<Food>();
	
	/** The food average position. */
	public static PVector foodAvg = new PVector(0, 0);
	
	/** The creatures. */
	public static ArrayList<Creature> creatures = new ArrayList<Creature>();

	/** The scent map. */
	public static QTree scentMap;

	/** The genetics. */
	public static LSystem genetics;
	
	public static PGraphics renderBuffer;
	
	public PImage cellsAPass;
	
	/* JS Console */
	public JSConsole jsConsole;
	
	public static boolean pause = false;

	private static Method callAfterRender;

	private static Object[] callArgumentsAfterRender;
	
	public static void reset(){
		scentMap = new QTree(0, 0, FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, 1);
		genetics = new LSystem("ab", app);

		theCells = new ArrayList<Cell>();
		aCells = new ArrayList<CellA>();
		bCells = new ArrayList<CellB>();
		growingCells = new ArrayList<Cell>();

		foods = new ArrayList<Food>();
		foodAvg = new PVector(0, 0);
		creatures = new ArrayList<Creature>();
		
		//Good idea to clean up the memory at this point
		System.gc();
	}
	
	public Mimodek(PApplet app, int facadeType){
		Mimodek.app = app;
		
		
		// setup facade
		FacadeFactory.createFacade(facadeType, app).border = 10f;
		
		renderBuffer = app.createGraphics(FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, PApplet.P3D);
		
		app.textureMode(PApplet.NORMAL);
		app.colorMode(PApplet.RGB, 1.0f);
		app.strokeCap(PApplet.SQUARE);
		//app.imageMode(PApplet.CENTER);
		
		// load color range
		try{
			TemperatureColorRanges.createTemperatureColorRanges(app,/*PAppletsettingsFolder.getAbsolutePath()+File.separatorChar+*/"settings/MimodekColourRanges.txt");
		}catch(Exception e){
			Verbose.overRule(e.getMessage());
		}
		
		Configurator.createConfigurator(app);
		Configurator.loadFromFile("settings/settings.xml");
		Verbose.speak = Configurator.getBooleanSetting("DEBUG_FLAG");
		
		// tracking client
		tuioClient = new TUIOClient(app);
		tuioClient.setListener(this);
		
		// Test that we can get a weather station for this location
		// NOTE: this test has a side effect of setting the starting values for
		// the weather variable, neat!
		MimodekLocation location = new MimodekLocation();
		location.city = Configurator.isSettingSet("LOCATION_CITY_STR") ? Configurator
				.getStringSetting("LOCATION_CITY_STR") : null;
		location.country = Configurator.isSettingSet("LOCATION_COUNTRY_STR") ? Configurator
				.getStringSetting("LOCATION_COUNTRY_STR") : null;
		location.latitude = Configurator.isSettingSet("LOCATION_LATITUDE") ? Configurator
				.getFloatSetting("LOCATION_LATITUDE") : null;
		location.longitude = Configurator.isSettingSet("LOCATION_LONGITUDE") ? Configurator
				.getFloatSetting("LOCATION_LONGITUDE") : null;
		Verbose.overRule("Hello " + location + "!");

		try{
			//DataHandler.testDataSource(app, location);	
			// data update thread
			dataHandler = DataHandler.getInstance(location, app);
		}catch(Exception e){
			//Verbose.debug("No weather station has been found for "+location+". Try the closest biggest city perhaps? \nMimodek will stop.");
			app.exit(); // no weather station so Mimodek can't run, too bad....
		}
		// in the future, more variables could be mapped
		dataHandler.map("temp_c", "DATA_TEMPERATURE");
		dataHandler.map("relative_humidity", "DATA_HUMIDITY");
		
		// Create geometries
		
		CellA.createShape( app.g, app.loadImage("textures/hardcell.png"));
		CellB.createShape( app.g, app.loadImage("textures/softcell.png"));
		
		// create and register data interpolators
		/*
		CellA.temperatureInterpolator = new TemperatureDataInterpolator(
				dataHandler);
		*/
		CellA.humidityInterpolator = new DataInterpolator("DATA_HUMIDITY",
				dataHandler);
		CellA.temperatureInterpolator = new TemperatureDataInterpolator(
				dataHandler);
		
		CellB.pollutionInterpolator = new PollutionDataInterpolator(dataHandler);
		CellB.temperatureInterpolator = new DataInterpolator(
				"DATA_TEMPERATURE", dataHandler);
		dataHandler.start();
		
		//set zero state
		reset();

		CellA a = new CellA(new PVector(FacadeFactory.getFacade().halfWidth*0.85f,
				FacadeFactory.getFacade().halfHeight), 1f);
		a.maturity = 1.0f;
		aCells.add(a);
		theCells.add(a);
		Creature.createHighLanderCreature(true);
		Creature.createHighLanderCreature(true);
		Creature.createHighLanderCreature(true);
		//reset = false;
		
		//Init the renderer
		Renderer.setup(app);
		Lighting.setup(app, 256);
		Background.setup(app, app.color(0,0,0,0));
		
		//Init navigation
		Navigation.setup();
		
		//Create JavaScript console
		jsConsole = new JSConsole();
		//AGive access to the configurator from the JS console through a <b>Mimodek</b> object
		jsConsole.runCommand("Mimodek = Java.type('mimodek2.Configurator')", true);
		jsConsole.runCommand("IO = Java.type('mimodek2.serializer.LoaderSaver')", true);
		//jsConsole.bindToJs("Mimodek", Configurator.getInstance() );
		//(new Thread(jsConsole)).start();
	}
	
	/*
	 * Handle data incoming from the Tracking application
	 */
	/* (non-Javadoc)
	 * @see MimodekV2.tracking.TrackingListener#trackingEvent(MimodekV2.tracking.TrackingInfo)
	 */
	public void trackingEvent(TrackingInfo info) {
		if (info.type == TrackingInfo.UPDATE) {
			Food.dropFood(info.x + (-1f + (float) Math.random() * 2), info.y
					+ (-1f + (float) Math.random() * 2));
		}
	}
	
	public static void invokeAfterRender(Method method, Object[] args){
		callAfterRender = method;
		callArgumentsAfterRender = args;
	}
	
	/*
	 * Update the status, position, life, etc... of Mimodek's world
	 */
	/**
	 * Update.
	 */
	public void update() {
		if(pause){
			return;
		}
		
		if (true || autoFood /*&& app.frameCount % 3 == 0*/) {
			/*
			Food.dropFood(app.random(FacadeFactory.getFacade().width), app
					.random(FacadeFactory.getFacade().height));
			*/
			genetics.addFood();
		}


		scentMap.update();

		// Update cells
		for (int i = 0; i < theCells.size(); i++)
			theCells.get(i).update(app);

		for (int i = 0; i < growingCells.size(); i++) {
			growingCells.get(i).update(app);
		}

		// Update/Draw food
		for (int f = 0; f < foods.size(); f++) {
			Food food = foods.get(f);
			food.z -= 0.0005 + Math.random() * 0.001;
			if (food.z <= 0) {
				foodAvg.sub(food);
				foods.remove(food);
				f--;
			}
		}

		// Update/Draw creatures
		for (int c = 0; c < creatures.size(); c++) {
			Creature cr = creatures.get(c);
			cr.update();
			if (cr.energy <= 0f) {
				creatures.remove(cr);
				Food.dropFood(cr.pos.x, cr.pos.y);
				c--;
			}
		}
	}
	
	public void draw(){
		if(pause)
			return;
		
		//Init global drawing parameters
		renderBuffer.beginDraw();
		renderBuffer.hint(PApplet.DISABLE_DEPTH_MASK);
		renderBuffer.textureMode(PApplet.NORMAL);
		renderBuffer.colorMode(PApplet.RGB, 1.0f);
		renderBuffer.strokeCap(PApplet.SQUARE);
		
		renderBuffer.ortho(); //camera
		renderBuffer.background(0,0); //clear previous frame
		Navigation.applyTransform(renderBuffer); //set global transform
		
		
		//Render the food
		if( foods.size() > 0 ){
			renderBuffer.shader( Renderer.getFoodShader(), PApplet.POINTS );
			for(int i= 0; i < foods.size(); i++)
				Renderer.render( renderBuffer, foods.get(i) );
		}
		
		CellB cellB;
		//Render the stems
		if( bCells.size() > 0 ){
			
			for (int i = 0; i < bCells.size(); i++){
				cellB = bCells.get(i);
				if( cellB.creatureA != null && cellB.creatureB != null )
					continue; //only render the stems under the A Cells if the leaf is not being carried
				
				Renderer.renderWithoutShader( renderBuffer, bCells.get(i) );

			}
		}
		renderBuffer.endDraw();
		
		PImage foodPass = renderBuffer.get();
		
		//Render the cells
		renderBuffer.beginDraw();
		Navigation.applyTransform(renderBuffer);
		renderBuffer.shader( Renderer.getCellShader() );
		
		if (aCells.size() > 0) {
			
			Renderer.setUniforms(aCells.get(0));
			for (int i = 0; i < aCells.size(); i++){
				Renderer.setTime( (app.millis()+i*10f)/1000f * 0.5f );
				Renderer.render(renderBuffer, aCells.get(i));
			}
		}
		renderBuffer.endDraw();
		
		cellsAPass = renderBuffer.get(); 
		
		renderBuffer.beginDraw();
		Navigation.applyTransform(renderBuffer);
		renderBuffer.background(0,0);
		
		if (bCells.size() > 0){
			//render stems of carried leafs
			for (int i = 0; i < bCells.size(); i++){
				cellB = bCells.get(i);
				if( cellB.creatureA != null && cellB.creatureB != null )
					Renderer.renderWithoutShader( renderBuffer, bCells.get(i) );//only render the stems above the A Cells if the leaf is not being carried
			}
			
			//render leafs
			renderBuffer.shader( Renderer.getCellShader() );
			Renderer.setUniforms(bCells.get(0));
			for (int i = 0; i < bCells.size(); i++){
				Renderer.render(renderBuffer, bCells.get(i));
			}
			/*
			//render the stems (no custom shader)
			renderBuffer.resetShader();
			
			for (int i = 0; i < bCells.size(); i++)
				Renderer.renderWithoutShader( renderBuffer, bCells.get(i) );
			*/
		}
		
		renderBuffer.endDraw();
		
		app.resetShader();
		//app.image(Background.get(), 0, 0);
		
		
		
		app.image(foodPass, 0, 0);
		app.image(cellsAPass, 0, 0);
		
		/*
		//Shadow casting lights from creatures
		// Could be modulated by the distance of the creature to the organism
		for (int i=0; i < creatures.size(); i++ ){
			if(!creatures.get(i).hasFood)
				continue;
			Lighting.render(app, renderBuffer, creatures.get(i).pos);
		}
		
		app.resetShader();
		*/
		
		app.image(renderBuffer, 0, 0);
		
		//Render the creatures on top of everything
		Navigation.applyTransform(app.g);
		app.shader( Renderer.getCreatureShader() );
		for (int i=0; i < creatures.size(); i++ )
			Renderer.render(app.g, creatures.get(i) );
		//app.image( Lighting.computeShadowMap( renderBuffer ), 0, app.height/2);
	}
	
	public void callAfterRender() {
		if (callAfterRender == null) {
			return;
		}
		try {
			callAfterRender.invoke(null, callArgumentsAfterRender);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		callAfterRender = null;
		callArgumentsAfterRender = null;
	}
}
