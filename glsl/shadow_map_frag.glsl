#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PI 3.14

//Uniforms specific to this shader
uniform vec2 resolution;
uniform sampler2D texture; //texture

//Interpolated from the values computed by the vertex shader
varying vec4 vertColor;
varying vec4 vertTexCoord;

//alpha threshold for our occlusion map
const float THRESHOLD = 0.75;


//https://github.com/mattdesl/lwjgl-basics/wiki/2D-Pixel-Perfect-Shadows
void main() {
  float distance = 1.0;

  /*
  float theta = (PI * 2.) * (vertTexCoord.s/resolution.x); //the angle of the ray
  float cosTheta = cos(theta);
  float sinTheta = sin(theta);
  */
  for (float y=0.0; y<resolution.y; y+=1.0) {

    
        //rectangular to polar filter
        vec2 norm = vec2(vertTexCoord.s, y/resolution.y) * 2.0 - 1.0;
        float theta = PI*1.5 + norm.x * PI; 
        float r = (1.0 + norm.y) * 0.5;

        //coord which we will sample from occlude map
        vec2 coord = vec2(-r * sin(theta), -r * cos(theta))/2.0 + 0.5;
 
        //the current distance is how far from the top we've come
        float dst = y/resolution.y;
/*
        //Ray source at the center of the occlude map (at 0.5, 0.5 in normalized coords)
        float r = 0.5 * dst;
        vec2 coord = vec2(r * cosTheta, r * sinTheta) + 0.5;
*/
        //sample the occlusion map
        vec4 data = texture2D(texture, coord);


        //if we've hit an opaque fragment (occluder), then get new distance
        //if the new distance is below the current, then we'll use that for our ray
        float caster = data.a;
        if (caster > THRESHOLD) {
            distance = min(distance, dst);
            break;
            //NOTE: we could probably use "break" or "return" here
        }
  } 
  gl_FragColor = vec4(vec3(distance), 1.0);
}
