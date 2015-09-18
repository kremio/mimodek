#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D depthMask; //depth mask
uniform vec2 resolution; //dimensions in pixels of the depthMask
/*
  x,y: the position of the light in pixels
  z: the radius of the light in pixels
  w: the depth of the light
*/
uniform vec2 lightData;
uniform vec2 lightPosition;

//From default Processing texture shader
uniform sampler2D texture;

uniform vec2 texOffset;

varying vec4 vertPosition;
varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

 // float t = lightData.x/resolution.x ;//(lightData.x + ((vertTexCoord.s - 0.5)  * lightData.z)) / resolution.x;
  //Sample the depth mask at the position of the fragment
  //Convert to texture coordinates (0.0 to 1.0)
   //position of the current fragment texture coordinate in pixels
  //vec2 pixPos = vec2(vertTexCoord.s - 0.5, vertTexCoord.t - 0.5) * lightData.x;
  
  //(  vertTexCoord.st - vec2(0.5,0.5) ) * lightData.z;
  //position in the depth mask
//  vec4 depth = gl_FragCoord.z / gl_FragCoord.w;

  // Converting (x,y,z) to range [0,1]
/*
float x = gl_FragCoord.x/ (2.0*lightData.x);
float y = gl_FragCoord.y/  (2.0*lightData.x);
float z = gl_FragCoord.z; // Already in range [0,1]

// Converting from range [0,1] to NDC [-1,1]
float ndcx = x * 2.0 - 1.0;
float ndcy = y * 2.0 - 1.0;
float ndcz = z * 2.0 - 1.0;
vec3 ndc = vec3(ndcx, ndcy, ndcz);
*/

float x = (vertTexCoord.s - 0.5) * (2.0*lightData.x);
float y = (vertTexCoord.t - 0.5) * (2.0*lightData.x);

float fX = x + lightPosition.x;
float fY = y + lightPosition.y;
vec2 ndc = vec2(fX,fY);


  vec2 fragPos = ndc / resolution;//resolution; //position of the center of the light


//  gl_FragColor = texture2D(depthMask, fragPos.st );
  
  if( texture2D(depthMask, fragPos).r > lightData.y /* || texture2D(depthMask, fragPos).a == 0.0*/ ){ //this fragment of light is under something
//    gl_FragColor = vec4(0,1,0,1);
  //  return;
    discard;
  }

  gl_FragColor = texture2D(texture, vertTexCoord.st) * vertColor;

}
