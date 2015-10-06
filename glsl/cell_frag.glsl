#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

//Uniforms specific to this shader
uniform sampler2D mask; //alpha mask
uniform sampler2D texture; //texture
uniform sampler2D theTexture;

uniform bool depthOnly;
uniform float depth;
uniform vec4 cellColor;

//Interpolated from the values computed by the vertex shader
varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  //vec4 theColor = vec4(vertColor.rgb, alpha);

//  if(depth <= 0.){
    //Apply the alpha mask
    vec4 colorFromTexture = vec4( depthOnly ? vec3(1.0) : texture2D(theTexture, vertTexCoord.st).rgb, texture2D(mask, vertTexCoord.st).a);
//Multiply with the per vertex colour to tint the texture
    gl_FragColor = colorFromTexture * (depthOnly ? vec4(1.0) : cellColor) * depth;
/*
  }else{
    vec4 colorFromTexture = vec4(vec3(depth), texture2D(mask, vertTexCoord.st).a);
    gl_FragColor = colorFromTexture;
  }
*/
      
}
