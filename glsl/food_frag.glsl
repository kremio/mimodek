#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float sharpness;

varying vec4 vertColor;
varying vec2 texCoord;

void main() { 
  float dist = length(texCoord - vec2(0.5));
  
  if(  dist > 0.5 )
    discard;

  float len = max(0.25, 1.0 - dist / 0.5);//weight/2.0 - length(pos);
  vec4 color = vec4(1.0, 1.0, 1.0, len);
  color = mix(vec4(0.0), color, sharpness);		  
  color = clamp(color, 0.0, 1.0);		
  //gl_FragColor = color * vertColor; 
  gl_FragColor = color;
}
