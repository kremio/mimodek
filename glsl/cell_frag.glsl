#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

//Uniforms specific to this shader
uniform sampler2D mask; //alpha mask
uniform sampler2D texture; //texture
uniform sampler2D theTexture;

uniform float depth;

//Interpolated from the values computed by the vertex shader
varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  if(depth <= 0.){
    //Apply the alpha mask
    vec4 colorFromTexture = vec4(texture2D(theTexture, vertTexCoord.st).rgb, texture2D(mask, vertTexCoord.st).a);
//Multiply with the per vertex colour to tint the texture
    gl_FragColor = colorFromTexture * vertColor;

  }else{
    vec4 colorFromTexture = vec4(vec3(depth), texture2D(mask, vertTexCoord.st).a);
    gl_FragColor = colorFromTexture;
  }

      
}
