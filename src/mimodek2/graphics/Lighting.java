package mimodek2.graphics;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PShader;

public class Lighting {

	private static int lightSize;
	private static int halfLightSize;
	/*
	 * 1D grayscale texture to store the rays length:
	 * coordinate equals the rays angle, the value is the normalized ray length
	 */
	private static PGraphics shadowMap;
	private static PShader shadowMapShader;
	
	private static PShader lightShader;
	//private static PImage lightImage;
	//private static PGraphics litArea;
	
	public static void setup(PApplet app, int lightSize){
		Lighting.lightSize = lightSize;
		halfLightSize = lightSize >> 1;
		//Create the buffer that will be used to compute the shadow map for each frame
		shadowMap = app.createGraphics(lightSize, 1, PApplet.P3D);
		
		//Create a buffer to render the lit area
		//litArea = app.createGraphics(lightSize, lightSize, PApplet.P3D);
		
		//load the shader that computes the shadow map from the occluders texture
		shadowMapShader = app.loadShader("glsl/shadow_map_frag.glsl");
		shadowMapShader.set("resolution", (float)lightSize, (float)lightSize);
		
		//load the shader that renders the light from the shadow map
		lightShader = app.loadShader("glsl/light_frag.glsl");
		lightShader.set("resolution", (float)lightSize, (float)lightSize);
		
		//lightImage = app.createImage(lightSize, lightSize, PApplet.ARGB);
	}
	
	public static PImage computeShadowMap(PImage occludersTexture, float x, float y){
		//extract the lit area
		/*
		
		litArea.beginDraw();
		litArea.background(0,0);
		//move to center of light
		litArea.translate(-occludersTexture.width/2 - lightSize * 0.5f, -occludersTexture.width/2 - lightSize * 0.5f);
		//draw to the buffer
		litArea.image(occludersTexture, 0, 0);
		litArea.endDraw();
		*/
		//occludersTexture.updatePixels(occludersTexture.width/2 - halfLightSize, occludersTexture.height/2 - halfLightSize, lightSize, lightSize);
		occludersTexture.loadPixels();
		PImage litArea = occludersTexture.get(PApplet.round(x - halfLightSize), PApplet.round(y - halfLightSize), lightSize, lightSize);
		//return litArea;
		
		shadowMap.beginDraw();
		shadowMap.textureMode(PApplet.NORMAL);
		shadowMap.colorMode(PApplet.RGB, 1.0f);
		shadowMap.background(0,0);
		
		shadowMap.shader(shadowMapShader);
		shadowMap.image(litArea, 0, 0);
		
		shadowMap.endDraw();
		
		return shadowMap;
		
	}
	
	public static void render(PApplet app, PImage occluderTexture){
		render( app, occluderTexture, occluderTexture.width/2, occluderTexture.height/2);
	}
	
	public static void render(PApplet app, PImage occluderTexture, PVector pos){
		render( app, occluderTexture, pos.x, pos.y);
	}
	
	public static void render(PApplet app, PImage occluderTexture, float x, float y){
		PImage shadowTexture = computeShadowMap( occluderTexture, x , y );
		app.shader( lightShader );
		
		app.pushMatrix();
		app.translate(x, y);
		app.noStroke();
		app.beginShape();
		app.texture( shadowTexture );
		app.vertex( lightSize * -0.5f, lightSize * -0.5f, 0f, 0f);
		app.vertex( lightSize * 0.5f, lightSize * -0.5f, 1f, 0f);
		app.vertex( lightSize * 0.5f, lightSize * 0.5f, 1f, 1f);
		app.vertex( lightSize * -0.5f, lightSize * 0.5f, 0f, 1f);
		
		app.endShape();
		
		app.popMatrix();
	}
	
}
