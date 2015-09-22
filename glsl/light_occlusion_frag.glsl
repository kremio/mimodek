#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D lightTexture; //depth mask
uniform float lightData; //dimensions in pixels of the depthMask


//From default Processing texture shader
uniform sampler2D texture;

uniform vec2 texOffset;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  
  if( texture2D(texture, vertTexCoord.st).r > lightData /* || texture2D(depthMask, fragPos).a == 0.0*/ ){
    discard;
  }

  gl_FragColor = texture2D(lightTexture, vertTexCoord.st) * vertColor;

}
