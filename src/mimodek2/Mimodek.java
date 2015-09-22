
package mimodek2;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import juttu.jsconsole.JSConsole;
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

	private TUIOClient tuioClient;
	
	/** The cells. */
	public static ArrayList<Cell> theCells = new ArrayList<Cell>();
	
	/** The a cells. */
	public volatile static ArrayList<CellA> aCells = new ArrayList<CellA>();
	
	/** The b cells. */
	public volatile static ArrayList<CellB> bCells = new ArrayList<CellB>();
	
	/** The growing cells. */
	public static ArrayList<Cell> growingCells = new ArrayList<Cell>();

	/** The foods. */
	public volatile static ArrayList<Food> foods = new ArrayList<Food>();
	
	/** The food average position. */
	public static PVector foodAvg = new PVector(0, 0);
	
	/** The creatures. */
	public volatile static ArrayList<Creature> creatures = new ArrayList<Creature>();

	/** The scent map. */
	public static QTree scentMap;

	/** The genetics. */
	public static LSystem genetics;
	
	public static PGraphics renderBuffer;
	public static PGraphics backgroundBuffer;
	
	public PImage cellsAPass;
	
	/* JS Console */
	public JSConsole jsConsole;
	
	public static float bgColor = 0.33f;
	
	public static boolean hideBg = false;

	public static void setBgColor(float r, float g, float b){
		bgColor = app.color(r,g,b);
	}
	
	/* Post render hook callback */
	private static Method callAfterRender;
	private static Object[] callArgumentsAfterRender;
	
	/* For sorting leaves at render time */
	private ArrayList<CellB> leaves = new ArrayList<CellB>();
	private ArrayList<CellB> deadLeaves = new ArrayList<CellB>();
	private ArrayList<CellB> carriedLeaves = new ArrayList<CellB>();
	
	private static Thread updateThread;
	
	class UpdateRunnable implements Runnable{
		
		public void run() {
			while(true){
				scentMap.update();
			}
		}
		
	}
	
	public static float fps(){
		return app.frameRate;
	}
	
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
		CellB.createShape( app.g, app.loadImage("textures/softcell.png"));
		
		// create and register data interpolators
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
		//Background.setup(app, app.color(0,0,0,0));
		
		//Init navigation
		Navigation.setup();
		
		//Create JavaScript console
		try {
			jsConsole = new JSConsole("js/InitConsole.js");
			jsConsole.runCommand("help()");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateThread = new Thread(new UpdateRunnable( ));
		updateThread.start();
		
		
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
		

		
		if ( Configurator.getBooleanSetting("AUTO_FOOD") ) {
			//randomly add food
			genetics.addFood();
		}

		// Update cells
		for (int i = 0; i < theCells.size(); i++)
			theCells.get(i).update(app);

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
		for (int c = 0; c < creatures.size(); c++) {
			Creature cr = creatures.get(c);
			cr.update();
			if (cr.energy <= 0f) {
				creatures.remove(cr);
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
		if(app.frameCount > 100 && app.frameRate < 25f){
			//Find the cellAs with the lowest level
			
			//propagate the change in levels through the structure
			CellA rootCell = CellA.unRootCells();
			ArrayList<CellB> rootLeaves = CellB.unRootLeaves();
			
			//remove them from the active cells, with their bCells and draw to background
			drawToBackground( rootCell, rootLeaves );
			
			
			
			System.out.println("Removed one cell and "+rootLeaves.size()+" leaves.");

		}
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
		if( bCells.size() > 0 ){
			for (CellB cellB : bCells){
				if( cellB.moving || cellB.eatable){ //sort the leaves
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
				Renderer.setTime( (time+cellA.id*10f)/1000f * 0.5f );
				Renderer.render(renderBuffer, cellA);
				
			}
		}
		
		if (leaves.size() > 0){
			
			Renderer.setUniforms(bCells.get(0));
			for (CellB cellB : leaves){
				if( cellB.moving || cellB.eatable )
					continue;
				Renderer.render(renderBuffer, cellB);
			}
		}
		
		/* Render the leafs being carried */
		if (carriedLeaves.size() > 0){
			//render stems of carried leafs
			renderBuffer.resetShader();
			for (CellB cellB : carriedLeaves){
				if( cellB.creatureA != null && cellB.creatureB != null )
					Renderer.renderWithoutShader( renderBuffer, cellB );//only render the stems above the A Cells if the leaf is not being carried
			}
			
			//render leafs
			renderBuffer.shader( Renderer.getCellShader() );
			for (CellB cellB : carriedLeaves){
				Renderer.render(renderBuffer, cellB);
			}

		}
		
		renderBuffer.endDraw();
		
		
		/* Compose the final image from the multiple passes */
		app.resetShader();
		app.pushMatrix();
		Navigation.applyTransform(app.g);
		if( !hideBg )
			//Draw the background
			app.image(backgroundBuffer,0,0);
		
		//Render the dead leaves
		if(deadLeaves.size() > 0){
			app.shader( Renderer.getCellShader() );
			Renderer.setUniforms(deadLeaves.get(0));
			for(CellB cellB : deadLeaves)
				Renderer.render(app.g, cellB);
		}
		
		//Render the food
		if (foods.size() > 0) {
			app.shader(Renderer.getFoodShader(), PApplet.POINTS);
			for (int i = 0; i < foods.size(); i++)
				Renderer.render(app.g, foods.get(i));
		}
		
		app.popMatrix();
		
		//Render the living cells
		app.resetShader();
		
		
		app.image(renderBuffer, 0, 0);
		
		
		
		
		
		//Render the creatures on top of everything
		Navigation.applyTransform(app.g);
		
		app.blendMode(PApplet.ADD);
		
		//TODO: try rendering in a buffer
		app.tint(0.5f);
		 
		app.resetShader();

		for (Creature creature : creatures)
			Renderer.renderLight(app.g, creature);

		app.noTint();
		
		app.blendMode(PApplet.BLEND);
		
		app.shader( Renderer.getCreatureShader() );
		for (Creature creature : creatures)
			Renderer.render(app.g, creature );
		
		//De-reference
		leaves.clear();
		deadLeaves.clear();
		carriedLeaves.clear();
		
		//Run callback, if any
		callAfterRender();
	}
	
	private void drawToBackground(CellA rootCell, ArrayList<CellB> rootLeaves){
		
		float time = app.millis();
		
		//Init global drawing parameters
		backgroundBuffer.beginDraw();
		backgroundBuffer.hint(PApplet.DISABLE_DEPTH_MASK);
		backgroundBuffer.textureMode(PApplet.NORMAL);
		backgroundBuffer.colorMode(PApplet.RGB, 1.0f);
		backgroundBuffer.strokeCap(PApplet.SQUARE);
		
		backgroundBuffer.ortho(); //camera
		backgroundBuffer.noStroke();
		backgroundBuffer.fill(0,0.0001f);
		backgroundBuffer.rect(0,0, backgroundBuffer.width, backgroundBuffer.height); //slightly faint previous layer
		
		/* Render the stems */
		if( rootLeaves.size() > 0 ){
			for (CellB cellB : rootLeaves){
				//De-reference
				Mimodek.bCells.remove(cellB);
				Mimodek.theCells.remove(cellB);
				Renderer.renderWithoutShader( backgroundBuffer, cellB );
			}
		}
		
		/* Render the cell */
		backgroundBuffer.shader( Renderer.getCellShader() );		
			
		Renderer.setUniforms(rootCell);

		// De-reference
		Mimodek.aCells.remove(rootCell);
		Mimodek.theCells.remove(rootCell);

		Renderer.setTime((time + rootCell.id * 10f) / 1000f * 0.5f);
		Renderer.render(backgroundBuffer, rootCell);
				
		
		if (rootLeaves.size() > 0){
			
			Renderer.setUniforms(rootLeaves.get(0));
			for (CellB cellB : rootLeaves){
				Renderer.render(backgroundBuffer, cellB);
			}
		}
		
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
}
