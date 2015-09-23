package mimodek2.graphics;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;
import mimodek2.Configurator;
import mimodek2.bio.*;


public class Renderer {
	
	private static PShader cellShader;
	private static PImage cellA_MaskTexture;
	private static PImage cellB_MaskTexture;
	
	private static PShader creatureShader;
	public static PImage creatureTexture;
	
	private static PShader foodShader;
	private static PShader lightOcclusionShader;
	
	public static void setup(PApplet app){
		cellShader = app.loadShader("glsl/cell_frag.glsl", "glsl/cell_vert.glsl");
		cellA_MaskTexture = app.loadImage("textures/hardcell_mask.png");
		cellB_MaskTexture = app.loadImage("textures/softcell_mask.png");
		
		//General shader settings
		cellShader.set("amplitude", 0.1f); //Configurator.getFloatSetting("CELLA_DISTORTION");
		cellShader.set("time", 0f);
		cellShader.set("depth", 0f);
		createCreatureTexture(app, 0.5f);
		creatureShader = app.loadShader("glsl/creature_frag.glsl", "glsl/creature_vert.glsl");
		creatureShader.set("sprite", creatureTexture);
	
		foodShader = app.loadShader("glsl/food_frag.glsl", "glsl/food_vert.glsl");
		foodShader.set("sharpness", 0.5f);
		
		lightOcclusionShader = app.loadShader("glsl/light_occlusion_frag.glsl"/*,"glsl/light_occlusion_vert.glsl"*/);
		//lightOcclusionShader.set("resolution", (float)app.width, (float)app.height);
		lightOcclusionShader.set("lightTexture", creatureTexture );
		System.out.println("All shaders were loaded.");
	}
	
	/*
	 Generate the texture for the creatures
	 */
	protected static void createCreatureTexture(PApplet app, float STRENGTH) {
		PGraphics gfx = app.createGraphics(512, 512, PApplet.P2D);
		gfx.beginDraw();
		//gfx.background(0);
		gfx.loadPixels();
		for (int i = 0; i < gfx.width; i++) {
			for (int j = 0; j < gfx.height; j++) {
				float v = equation(app, gfx.width, gfx.height, i, j, STRENGTH);
				// v = v > 0.95 * STRENGTH ? 1 : v / 2;
				if (v == 0)
					gfx.pixels[i + j * gfx.width] = gfx.color(0, 0, 0, 0);
				else
					gfx.pixels[i + j * gfx.width] = gfx.lerpColor(gfx.color(0), gfx.color(255), v);
			}
		}
		gfx.updatePixels();
		gfx.endDraw();
		
		creatureTexture = gfx.get();
		gfx.dispose();
	}

	/**
	 * Equation.
	 *
	 * @param app the app
	 * @param w the w
	 * @param h the h
	 * @param x the x
	 * @param y the y
	 * @param strength the strength
	 * @return the float
	 */
	protected static float equation(PApplet app, float w, float h, float x,
			float y, float strength) {
		float d = PApplet.dist(w / 2, h / 2, x, y);
		if (d > w / 2) {
			return 0;
		}
		if (d < w / 16) {
			return strength;
		}
		return (1 - (d / (w / 2))) * strength;
	}
	
	public static PShader getCellShader() {
		return cellShader;
	}
	
	public static PShader getCreatureShader(){
		return creatureShader;
	}
	
	public static PShader getFoodShader(){
		return foodShader;
	}
	
	public static PShader getLightShader(){
		return lightOcclusionShader;
	}

	//Set the shader's uniforms for each type of cells
	public static void setUniforms(CellA cellA){
		cellShader.set("mask", cellA_MaskTexture);
		cellShader.set("noDeform", false);
		cellShader.set("theTexture", CellA.texture);
		cellShader.set("depth", 1.0f);
		cellShader.set("depthOnly", false);
	}
	
	public static void setUniforms(Leaf cellB){
		cellShader.set("mask", cellB_MaskTexture);
		cellShader.set("noDeform", true);
		cellShader.set("theTexture", Leaf.texture);
		cellShader.set("depth", 1.0f);
		cellShader.set("depthOnly", false);
	}
	
	public static void setTime(float t){
		cellShader.set("time", t);
	}
	
	/*
	public static void renderDepth(PGraphics renderBuffer, CellA cellA){
		cellShader.set("depth", (float)cellA.level / (float)CellA.maxLevel );
		
		render(renderBuffer, cellA);
	}
	*/
	
	public static void setShaderColorUniform(PShader shader, String uniformName, int rgb255, float alpha){
		shader.set(uniformName, (rgb255 >> 16 & 0xFF)/255f, (rgb255 >> 8 & 0xFF)/255f, (rgb255 & 0xFF)/255f, alpha );
	}
	
	public static void render(PGraphics renderBuffer, CellA cellA, boolean depthPass){
		float depth = cellA.depth();
		
		renderBuffer.pushStyle();
		
		if( !depthPass ){		
			//Set the tint color
			int c = mimodek2.data.TemperatureColorRanges.getColor( CellA.temperatureInterpolator.getInterpolatedValue( PApplet.lerp(1f,0f,(float)cellA.level/(float)CellA.maxLevel) ) );
			setShaderColorUniform(cellShader, "cellColor", c, Configurator.getFloatSetting("CELLA_ALPHA") );
		}
		cellShader.set("depthOnly", depthPass);
		cellShader.set("depth", depth);
		
		//Place and rotate the cell
		renderBuffer.pushMatrix();
		Cell anchor = cellA.anchor; 
		if ( anchor != null ) {
			renderBuffer.translate(anchor.pos.x + cellA.currentMaturity * (cellA.pos.x - anchor.pos.x), 
					anchor.pos.y + cellA.currentMaturity * (cellA.pos.y - anchor.pos.y),
					cellA.level);
		} else {
			renderBuffer.translate(cellA.pos.x, cellA.pos.y, cellA.level);
		}

		renderBuffer.rotate( cellA.aa, 0.0f,0.0f,1.0f);
		renderBuffer.scale(cellA.currentMaturity*cellA.radius()*2.0f, cellA.currentMaturity*cellA.radius()*2.0f, 1f);
		
		//Render
		renderBuffer.shape( CellA.shape );

		renderBuffer.popMatrix();
		
		renderBuffer.popStyle();
	}
	
	public static void render(PGraphics renderBuffer, CellA cellA) {
		render(renderBuffer, cellA, false);
	}
	
	public static void renderWithoutShader(PGraphics renderBuffer, Leaf cell){
		Cell anchor = cell.anchor;
		
		renderBuffer.pushStyle();
		
		//Draw the stem	
		if (cell.currentMaturity > 0.4f) {
			renderBuffer.strokeWeight(1.0f);
			renderBuffer.stroke(1.0f);
			renderBuffer.noFill();
			if (cell.creatureA != null && cell.creatureB != null
					&& cell.creatureA.readyToLift && cell.creatureB.readyToLift) {
				renderBuffer.line(anchor.pos.x + cell.currentMaturity
						* (cell.pos.x - anchor.pos.x), anchor.pos.y
						+ cell.currentMaturity * (cell.pos.y - anchor.pos.y),
						anchor.pos.x, anchor.pos.y);
			} else if (cell.edible) {
				renderBuffer.line(anchor.pos.x + cell.currentMaturity
						* (cell.pos.x - anchor.pos.x), anchor.pos.y
						+ cell.currentMaturity * (cell.pos.y - anchor.pos.y),
						cell.zLevel,
						anchor.pos.x + (cell.pos.x - anchor.pos.x),
						anchor.pos.y + (cell.pos.y - anchor.pos.y), cell.zLevel);
			} else {
				renderBuffer.line(anchor.pos.x + cell.currentMaturity
						* (cell.pos.x - anchor.pos.x), anchor.pos.y
						+ cell.currentMaturity * (cell.pos.y - anchor.pos.y),
						cell.zLevel, anchor.pos.x + 0.4f
								* (cell.pos.x - anchor.pos.x), anchor.pos.y
								+ 0.4f * (cell.pos.y - anchor.pos.y),
						cell.zLevel);
			}

		}
		renderBuffer.popStyle();
	}
	
	public static void render(PGraphics renderBuffer, Leaf cell){
		render(renderBuffer, cell, false);
	}
	
	public static void render(PGraphics renderBuffer, Leaf cell, boolean depthPass){
		
		renderBuffer.pushStyle();
		
		Cell anchor = cell.anchor;
		if(cell.creatureA !=null && cell.creatureB !=null && cell.creatureA.readyToLift && cell.creatureB.readyToLift){
			cell.zLevel = CellA.maxLevel;
		}else if(!cell.edible){
			cell.zLevel = anchor.zLevel;
		}else{
			cell.zLevel = 0;
		}
		
		//Compute cell color
		int c;
		if(cell.edible){
			float step = 1f;
			if(cell.maturity>0.5f){
				step = 1f-((cell.maturity-0.5f)/0.5f);
			}
			int deadColor = renderBuffer.color(0.8f);
			c = renderBuffer.lerpColor(cell.color,deadColor,step);
			cellShader.set("depth", CellA.MIN_DEPTH);
		}else{
			
			float depth = cell.moving ? CellA.MAX_DEPTH : ((CellA)anchor).depth();
			cellShader.set("depth", depth);
			c = cell.color;	
		}
		
		cellShader.set("depthOnly", depthPass);
		
		if( !depthPass ){
			//Set the tint color
			setShaderColorUniform(cellShader, "cellColor", c, cell.currentBrightness );
		}
		
		//Draw the leaf
		renderBuffer.pushMatrix();
		float tX = anchor.pos.x + cell.currentMaturity * (cell.pos.x - anchor.pos.x) /* 1.5f*/;
		float tY = anchor.pos.y + cell.currentMaturity * (cell.pos.y - anchor.pos.y) /* 1.5f*/;

		renderBuffer.translate(tX,tY,cell.zLevel);
		renderBuffer.rotate( PConstants.HALF_PI+cell.currentAngle,0.0f,0.0f,1.0f);
		renderBuffer.scale(cell.radius() * cell.currentMaturity , cell.radius() * cell.currentMaturity, 1f);
		
		renderBuffer.shape( Leaf.shape );
		
		renderBuffer.popMatrix();
		
		renderBuffer.popStyle();
	}
	
	
	public static void render(PGraphics renderBuffer, Lightie creature){
		float s = Configurator.getFloatSetting("CREATURE_SIZE")
				* Configurator.getFloatSetting("GLOBAL_SCALING");
		
		renderBuffer.pushStyle();
		
		renderBuffer.noLights();
		
		renderBuffer.blendMode(PApplet.ADD); //Switch to additive color blending
		
		creatureShader.set("weight", s);
		renderBuffer.strokeWeight(s);

		//app.pushMatrix();
		
		if (creature.hasFood) {
			renderBuffer.stroke(Configurator
					.getFloatSetting("CREATURE_FULL_R"), Configurator
					.getFloatSetting("CREATURE_FULL_G"), Configurator
					.getFloatSetting("CREATURE_FULL_B"), creature.currentBrightness);
		} else {
			renderBuffer.stroke(Configurator
					.getFloatSetting("CREATURE_R"), Configurator
					.getFloatSetting("CREATURE_G"), Configurator
					.getFloatSetting("CREATURE_B"), creature.currentBrightness);	
		}
		renderBuffer.point(creature.pos.x, creature.pos.y);
		
		//app.popMatrix();
		
		renderBuffer.blendMode(PApplet.BLEND); //Switch back to the default blending mode
		
		renderBuffer.popStyle();
	}
	
	public static void render(PGraphics renderBuffer, Food food){
		renderBuffer.pushStyle();
		
		renderBuffer.stroke(Configurator.getFloatSetting("FOOD_R"),
				Configurator.getFloatSetting("FOOD_G"),
				Configurator.getFloatSetting("FOOD_B"), 1f);
		renderBuffer.stroke(1f);
		foodShader.set("weight", /*food.z */ /*Configurator.getFloatSetting("FOOD_SIZE") */ 100f);
		//app.strokeWeight(500f /*food.z * Configurator.getFloatSetting("FOOD_SIZE")*/);
		renderBuffer.point(food.x, food.y);
		renderBuffer.popStyle();
	}
	
	public static void setDepthMask(PImage depthMask){
		lightOcclusionShader.set("depthMask", depthMask);
	}
	
	public static void renderLight(PGraphics renderBuffer, Lightie creature){
		float radius = PApplet.min(32f, creature.pos.z * 32f);

		if( radius <= 0f)
			return;

		renderBuffer.image(creatureTexture, creature.pos.x - radius, creature.pos.y - radius, radius*2, radius*2);
		
		
		
	}
	
	public static void renderLightCircle(PGraphics renderBuffer, Lightie creature){
		float radius = PApplet.min(32f, creature.pos.z * 32f);
		//app.text(radius, creature.pos.x,creature.pos.y);
		if( radius <= 0f)
			return;
		
		renderBuffer.pushStyle();
		renderBuffer.noStroke();
		renderBuffer.fill(creature.pos.z);
		renderBuffer.ellipse(creature.pos.x, creature.pos.y, 2*radius, 2*radius);
		renderBuffer.popStyle();
	}


}
