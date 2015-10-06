
package mimodek2;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import oscP5.OscMessage;
import juttu.jsconsole.JSConsole;
import mimodek2.tracking.*;
import mimodek2.tracking.osc.OSCom;
import mimodek2.tracking.osc.OscMessageListener;
import mimodek2.data.*;
import mimodek2.bio.*;
import mimodek2.facade.*;
import mimodek2.graphics.*;
import processing.core.*;

public class Mimodek implements OscMessageListener {
	
	/** The number of vertices making up the geometry of type A cells */
	public static int CELLA_VERTEX_NUMBER = 20;
	
	private static PApplet app;

	private DataHandler dataHandler;
	
	/** The cells. */
	public static ArrayList<Cell> allCells = new ArrayList<Cell>();
	
	/** The a cells. */
	public  static ArrayList<CellA> aCells = new ArrayList<CellA>();
	public volatile static ArrayList<CellA> cellsToFossilize = new ArrayList<CellA>();
	
	/** The b cells. */
	public volatile static ArrayList<Leaf> leavesCells = new ArrayList<Leaf>();
	
	/** The growing cells. */
	public static ArrayList<Cell> growingCells = new ArrayList<Cell>();

	/** The foods. */
	public  static ArrayList<Food> foods = new ArrayList<Food>();
	
	/** The food average position. */
	public static PVector foodAvg = new PVector(0, 0);
	
	/** The creatures. */
	public  static ArrayList<Lightie> lighties = new ArrayList<Lightie>();
	
	public  static ArrayList<HighLightie> highLighties = new ArrayList<HighLightie>();

	/** The scent map. */
	public static QTree scentMap;

	/** The genetics. */
	public static LSystem genetics;
	
	public static PGraphics renderBuffer;
	public static PGraphics backgroundBuffer;
	
	public PImage cellsAPass;
	
	/* JS Console */
	public JSConsole jsConsole;
	
	public static int lastFpsCheck = 100;
	
	/* Post render hook callback */
	private static Method callAfterRender;
	private static Object[] callArgumentsAfterRender;
	
	/* For sorting leaves at render time */
	private ArrayList<Leaf> leaves = new ArrayList<Leaf>();
	private ArrayList<Leaf> deadLeaves = new ArrayList<Leaf>();
	private ArrayList<Leaf> carriedLeaves = new ArrayList<Leaf>();
	
	private static Thread updateThread;
	
	public static OSCom oscCommunication;
	
	private static float lastOscData = 0;
	
	class UpdateRunnable implements Runnable{
		
		public void run() {
			while(true){
				scentMap.update();
			}
		}
		
	}
	
	public static void useRealData() throws Exception{
		DataHandler.getInstance(null, null).useRealData(app);
	}
	
	public static float fps(){
		return app.frameRate;
	}
	
	public static void reset(){
		scentMap = new QTree(0, 0, FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, 1);
		genetics = new LSystem("ab", app);

		allCells = new ArrayList<Cell>();
		aCells = new ArrayList<CellA>();
		leavesCells = new ArrayList<Leaf>();
		growingCells = new ArrayList<Cell>();

		foods = new ArrayList<Food>();
		foodAvg = new PVector(0, 0);
		lighties = new ArrayList<Lightie>();
		
		//Good idea to clean up the memory at this point
		System.gc();
	}
	
	public Mimodek(PApplet app, int facadeType){
		Mimodek.app = app;
		
		
		// setup facade
		FacadeFactory.createFacade(facadeType, app).border = 10f;
		
		renderBuffer = app.createGraphics(FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, PApplet.P3D);
		backgroundBuffer = app.createGraphics(FacadeFactory.getFacade().width, FacadeFactory.getFacade().height, PApplet.P3D);
		
		app.textureMode(PApplet.NORMAL);
		app.colorMode(PApplet.RGB, 1.0f);
		app.strokeCap(PApplet.SQUARE);
		//app.imageMode(PApplet.CENTER);
		
		// load color range
		try{
			TemperatureColorRanges.createTemperatureColorRanges(app,"settings/MimodekColourRanges.txt");
		}catch(Exception e){
			Verbose.overRule(e.getMessage());
		}
		
		
		
		// tracking client
		//tuioClient = new TUIOClient(app);
		//tuioClient.setListener(this);
		
		//OSC
		oscCommunication = new OSCom(3000, "localhost", 3001);
		
		
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
			DataHandler.testDataSource(app, location);	
			// data update thread
			dataHandler = DataHandler.getInstance(location, app);
		}catch(Exception e){
			e.printStackTrace();
			//Verbose.debug("No weather station has been found for "+location+". Try the closest biggest city perhaps? \nMimodek will stop.");
			System.exit(0);
			//app.exit(); // no weather station so Mimodek can't run, too bad....
		}
		/* Setup data to configuration mapping */
		// in the future, more variables could be mapped
		dataHandler.map("temp_c", "DATA_TEMPERATURE");
		dataHandler.map("relative_humidity", "DATA_HUMIDITY");
		
		// Create geometries
		CellA.createShape( app.g, app.loadImage("textures/hardcell.png"));
		Leaf.createShape( app.g, app.loadImage("textures/softcell.png"));
		
		// create and register data interpolators
		CellA.humidityInterpolator = new DataInterpolator("DATA_HUMIDITY",
				dataHandler);
		CellA.temperatureInterpolator = new TemperatureDataInterpolator(
				dataHandler);
		
		Leaf.pollutionInterpolator = new PollutionDataInterpolator(dataHandler);
		Leaf.temperatureInterpolator = new DataInterpolator(
				"DATA_TEMPERATURE", dataHandler);
		dataHandler.start();
		
		//set zero state
		reset();

		CellA a = new CellA(new PVector(FacadeFactory.getFacade().halfWidth*0.85f,
				FacadeFactory.getFacade().halfHeight), 1f);
		a.maturity = 1.0f;
		aCells.add(a);
		allCells.add(a);
		highLighties.add( HighLightie.spawn() );
		highLighties.add( HighLightie.spawn() );
		highLighties.add( HighLightie.spawn() );
		//reset = false;
		
		//Init the renderer
		Renderer.setup(app);
		
		//Init navigation
		Navigation.setup();
		
		//Create JavaScript console
		try {
			jsConsole = new JSConsole("js/InitConsole.js", !Configurator.getBooleanSetting("FULLSCREEN_FLAG") );
			jsConsole.runCommand("help()");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateThread = new Thread(new UpdateRunnable( ));
		updateThread.start();
		
		oscCommunication.addListener("/mimodek/activity/", this);
		
		
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
		Tween.updateTweens();
		
		//True death loop
		for (int c = 0; c < aCells.size(); c++){
			CellA cellA = aCells.get(c);
			if(cellA.level == 0f){
				Mimodek.aCells.remove(cellA);
				Mimodek.allCells.remove(cellA);
			}
		}
		
		//try to remove leaves that could not be removed before due to a shortage of lighties
		Leaf[] toBePickedUp = (Leaf[])Leaf.toBePickedUp.toArray(new  Leaf[0]);
		for(Leaf leaf: toBePickedUp){
			leaf.makeEdible();
		}
		
		if ( Configurator.getBooleanSetting("AUTO_FOOD") ) {
			//randomly add food
			//genetics.addFood();
		}
		
		

		// Update cells
		for (int c = 0; c < allCells.size(); c++)
			allCells.get(c).update(app);
		
		

		// Update food
		for (int f = 0; f < foods.size(); f++) {
			Food food = foods.get(f);
			food.z -= 0.0005 + Math.random() * 0.001;
			if (food.z <= 0) {
				foodAvg.sub(food);
				foods.remove(food);
				f--;
			}
		}

		// Update creatures
		for (int c = 0; c < lighties.size(); c++) {
			Lightie cr = lighties.get(c);
			cr.update();
			if ( cr.isDead() ) {
				lighties.remove(cr);
				Food.dropFood(cr.pos.x, cr.pos.y);
				c--;
			}
		}
		
		/*
		 * Homeostasis:
		 * Remove the oldest cells from active rendering
		 * in order to maintain a satisfying frame rate.
		 * The removed cells are added to the background.  
		 * 
		 */
		//Wait a few frames to get a more accurate fps reading
		if(app.frameCount > lastFpsCheck && app.frameRate < 25f){
			
			
			//First, let's try to et rid of some idle Lighties
			/*
			Lightie idleLightie = null;
			for(Lightie lightie : lighties){
				if( !lightie.amIBusy() ){ //that one is doing nothing!
					idleLightie = lightie;
					break;
				}
			}
			
			//Find someone to hunt it down
			if( idleLightie != null ){
				for(HighLightie hunter : highLighties)
					hunter.huntTarget(idleLightie);
				return;
			}
			*/
			
			lastFpsCheck = app.frameCount + 100;
			//Let's trim the structure a bit
			CellA.unRootCells();
		}
		
		fossilizeStructure();
	}
	
	public static void fossilizeStructure(){
		//Find the cellAs with the lowest level
		//propagate the change in levels through the structure
		if( cellsToFossilize.size() < 1 ){
			return;
		}
		
		//First check if some cells are done fading
		ArrayList<CellA> rootCells = new ArrayList<CellA>();
		ArrayList<Leaf> rootLeaves = Leaf.unRootLeaves();
		for(int c = 0; c < cellsToFossilize.size(); c++){
			CellA rootCell = cellsToFossilize.get(c);
			if(rootCell.level <= 0.5f){
				rootCells.add(rootCell);
				cellsToFossilize.remove(c);
				new Tween(rootCell, "level", 0f, 5*60000);
			}
		}
		
		
		//System.out.println("Removed one cell and "+rootLeaves.size()+" leaves.");
		
		//remove them from the active cells, with their bCells and draw to background
		drawToBackground( rootCells, rootLeaves );
	}
	
	public void draw(){
		
		float time = app.millis();
		
		app.blendMode(PApplet.BLEND);
		
		
		//Init global drawing parameters
		renderBuffer.beginDraw();
		renderBuffer.hint(PApplet.DISABLE_DEPTH_MASK);
		renderBuffer.textureMode(PApplet.NORMAL);
		renderBuffer.colorMode(PApplet.RGB, 1.0f);
		renderBuffer.strokeCap(PApplet.SQUARE);
		
		renderBuffer.ortho(); //camera
		renderBuffer.background(0,0); //clear previous frame
		
		
		
		
		Navigation.applyTransform(renderBuffer); //set global transform
		
		
		/*
		 * Layout (from bottom to top):
		 * - food
		 * - dead leafs
		 * - leafs
		 * - cells
		 * - carried leaf
		 * - creatures
		 */
		
		
		/* Render the stems */
		if( leavesCells.size() > 0 ){
			for (Leaf cellB : leavesCells){
				if( cellB.moving || cellB.edible){ //sort the leaves
					if( cellB.moving ){
						carriedLeaves.add( cellB );
					}else{
						deadLeaves.add( cellB );
					}
					continue; //only render the stems under the A Cells if the leaf is not being carried
				}
				leaves.add( cellB );
				Renderer.renderWithoutShader( renderBuffer, cellB );
			}
		}
		
		/* Render the cells */
		renderBuffer.shader( Renderer.getCellShader() );		
		
		if (aCells.size() > 0) {
			
			Renderer.setUniforms(aCells.get(0));
			
			for (CellA cellA: aCells){
				if(cellA.level <= 0.5f) //only used has space holder
					continue;
				Renderer.setTime( (time+cellA.id*10f)/1000f * 0.5f );
				Renderer.render(renderBuffer, cellA);
				
			}
		}
		
		if (leaves.size() > 0){
			
			Renderer.setUniforms(leavesCells.get(0));
			for (Leaf cellB : leaves){
				if( cellB.moving || cellB.edible )
					continue;
				Renderer.render(renderBuffer, cellB);
			}
		}
		
		/* Render the leafs being carried */
		if (carriedLeaves.size() > 0){
			//render stems of carried leafs
			renderBuffer.resetShader();
			for (Leaf cellB : carriedLeaves){
				if( cellB.carrierA != null && cellB.carrierB != null )
					Renderer.renderWithoutShader( renderBuffer, cellB );//only render the stems above the A Cells if the leaf is not being carried
			}
			
			//render leafs
			renderBuffer.shader( Renderer.getCellShader() );
			for (Leaf cellB : carriedLeaves){
				Renderer.render(renderBuffer, cellB);
			}

		}
		
		renderBuffer.endDraw();
		
		
		/* Compose the final image from the multiple passes */
		app.resetShader();
		app.pushMatrix();
		Navigation.applyTransform(app.g);
		
		// Draw the background
		app.image(backgroundBuffer, 0, 0);
		
		//Render the dead leaves
		if(deadLeaves.size() > 0){
			app.shader( Renderer.getCellShader() );
			Renderer.setUniforms(deadLeaves.get(0));
			for(Leaf cellB : deadLeaves)
				Renderer.render(app.g, cellB);
		}
		
		//Render the food
		if (foods.size() > 0) {
			app.shader(Renderer.getFoodShader(), PApplet.POINTS);
			for (int f = 0; f < foods.size(); f++)
				Renderer.render(app.g, foods.get(f));
		}
		
		app.popMatrix();
		
		//Render the living cells
		app.resetShader();
		
		
		app.image(renderBuffer, 0, 0);
		
		//Render the creatures on top of everything
		app.pushMatrix();
		Navigation.applyTransform(app.g);
		
		app.blendMode(PApplet.ADD);
		
		//TODO: try rendering in a buffer
		app.tint(0.5f);
		 
		app.resetShader();

		for (Lightie creature : lighties)
			Renderer.renderLight(app.g, creature);

		app.noTint();
		
		app.blendMode(PApplet.BLEND);
		
		app.shader( Renderer.getCreatureShader() );
		for (Lightie creature : lighties)
			Renderer.render(app.g, creature );
		
		//De-reference
		leaves.clear();
		deadLeaves.clear();
		carriedLeaves.clear();
		
		app.popMatrix(); //Render pass end
		
		if(Configurator.getBooleanSetting("SHOW_DATA_FLAG")){
			app.text("Observation at: "+WeatherUndergroundClient.getLatestTimestamp(), 20, 20, 1);
			app.text("Temperature: "+Configurator.getFloatSetting("DATA_TEMPERATURE"), 20, 40, 1);
			app.text("Humidity: "+Configurator.getFloatSetting("DATA_HUMIDITY"), 20, 60, 1);
			app.text("FPS: "+app.frameRate, 20, 90, 1);
			app.text("Cells:" + aCells.size(), 20, 110, 1);
			app.text("Leaves:" + leaves.size(), 20, 130, 1);
			app.text("Creatures:" + lighties.size(), 20, 150, 1);
			app.text("Osc:"+lastOscData, 20, 180);
		}
		
		//Run callback, if any
		callAfterRender();
	}
	
	private static void drawToBackground(ArrayList<CellA> rootCells, ArrayList<Leaf> rootLeaves) {

		float time = app.millis();

		// Init global drawing parameters
		backgroundBuffer.beginDraw();
		backgroundBuffer.hint(PApplet.DISABLE_DEPTH_MASK);
		backgroundBuffer.textureMode(PApplet.NORMAL);
		backgroundBuffer.colorMode(PApplet.RGB, 1.0f);
		backgroundBuffer.strokeCap(PApplet.SQUARE);
		backgroundBuffer.blendMode(PApplet.BLEND);
		backgroundBuffer.ortho(); // camera

		backgroundBuffer.pushStyle();
		backgroundBuffer.noStroke();
		backgroundBuffer.fill(0, 0.005f);
		backgroundBuffer.rect(0, 0, backgroundBuffer.width, backgroundBuffer.height); // slightly faint previous layer
		
		backgroundBuffer.popStyle();

		/* Render the stems */
		if (rootLeaves.size() > 0) {
			for (Leaf cellB : rootLeaves) {
				// De-reference
				Mimodek.leavesCells.remove(cellB);
				Mimodek.allCells.remove(cellB);
				Renderer.renderWithoutShader(backgroundBuffer, cellB);
			}
		}
		/* Render the cell */
		backgroundBuffer.shader(Renderer.getCellShader());
		if (rootCells.size() > 0) {

			Renderer.setUniforms(rootCells.get(0));

			for (CellA rootCell : rootCells) {
				// De-reference
				// Mimodek.aCells.remove(rootCell);
				// Mimodek.allCells.remove(rootCell);

				Renderer.setTime((time + rootCell.id * 10f) / 1000f * 0.5f);
				Renderer.render(backgroundBuffer, rootCell);
			}
		}

		if (rootLeaves.size() > 0) {

			Renderer.setUniforms(rootLeaves.get(0));
			for (Leaf cellB : rootLeaves) {
				Renderer.render(backgroundBuffer, cellB);
			}
		}
		backgroundBuffer.resetShader();
		backgroundBuffer.endDraw();
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

	/*
	 * Handle activity data coming from the Kinect app through an OSC channel
	 */
	public void handleMessage(OscMessage message) {
		if( message.arguments().length == 1)
			lastOscData = message.get(0).floatValue();
		//If the activity is high enough add some food
		if( message.arguments().length == 1 /*&& message.get(0).floatValue() > Configurator.getFloatSetting("ACTIVITY_THRESHOLD_FLOAT")*/ ){
			genetics.addFood();
			Food.dropFood( (float)Math.random() * FacadeFactory.getFacade().width, (float)Math.random() * FacadeFactory.getFacade().height);
		}
		
	}
}
