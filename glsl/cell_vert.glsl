#define PROCESSING_TEXTURE_SHADER

//Uniforms specific to this shader
uniform float time; //Used to animate the vertex displacement
uniform float amplitude; //Factor affecting how much the vertex is displaced
uniform bool noDeform; //False is vertex displacement is disabled

uniform mat4 transform;
uniform mat4 texMatrix;

attribute vec2 texCoord;
attribute vec4 position;
attribute vec3 normal;
attribute vec4 vertex;
attribute vec4 color;

/*
attribute vec4 ambient;
uniform vec3 lightAmbient[8];
*/

varying vec4 vertColor;
varying vec4 vertTexCoord;


const vec3 zero_vec3 = vec3(0.0);

/*
   NOISE FUNCTIONS
 */

//
// Description : Array and textureless GLSL 2D simplex noise function.
//      Author : Ian McEwan, Ashima Arts.
//  Maintainer : ijm
//     Lastmod : 20110822 (ijm)
//     License : Copyright (C) 2011 Ashima Arts. All rights reserved.
//               Distributed under the MIT License. See LICENSE file.
//               https://github.com/ashima/webgl-noise
// 

vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
{
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
      -0.577350269189626,  // -1.0 + 2.0 * C.x
      0.024390243902439); // 1.0 / 41.0
  // First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

  // Other corners
  vec2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

  // Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
      + i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m ;
  m = m*m ;

  // Gradients: 41 points uniformly over a line, mapped onto a diamond.
  // The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;

  // Normalise gradients implicitly by scaling m
  // Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

  // Compute final noise value at P
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

/*
   VERTEX DISPLACEMENT
 */

void main() {

  if( noDeform ){
    gl_Position = transform * vertex;
  }else{
    //Get a perlin noise value
    float noise = snoise(vertex.xy+time);

    //Displace the vertex
    vec4 v = vertex + vec4(vertex.xyz * noise * amplitude, 1.0);
    //Apply model view and projection transformation to the object coordinates
    gl_Position = transform * v; 
  }


  //Pass the tint colour (from the first ambient light source) 
  vertColor = color; //vec4(lightAmbient[0],1.0);

  //Rescales the texture coordinates
  vertTexCoord = texMatrix * vec4(texCoord.st, 1.0, 1.0);

}
