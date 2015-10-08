#define PROCESSING_POINT_SHADER

uniform mat4 projection;
uniform mat4 transform;
 
attribute vec4 vertex;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 texCoord;

void main() {
  vec4 clip = transform * vertex;
  gl_Position = clip + projection * vec4(offset, 0, 0);
  
  float weight = abs(offset.x)*2.0;
  texCoord = (vec2(0.5) + offset / weight);

  vertColor = color;
}
