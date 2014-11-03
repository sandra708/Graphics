#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)
// veck getEnvironmetLight(veck dir)

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];

//Camera Information
uniform vec3 worldCam;
uniform float exposure;

//Shader information
uniform float shininess;

varying vec4 worldPos;
varying vec3 fN;
varying vec2 fUV;

void main() {
	// TODO A4: Implement reflection mapping fragment shader
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	
	vec3 R = (2 * dot(V, N) * N) - V;
	
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

	for (int i = 0; i < numLights; i++) {
	  float r = length(lightPosition[i] - worldPos.xyz);
	  vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
	  vec3 H = normalize(L + V);

	  // calculate diffuse term
	  vec4 Idiff = getDiffuseColor(fUV) * max(dot(N, L), 0.0);
	  Idiff = clamp(Idiff, 0.0, 1.0);

	  // calculate specular term
	  vec4 Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);
	  Ispec = clamp(Ispec, 0.0, 1.0);

	  finalColor += vec4(lightIntensity[i], 0.0) * (Idiff + Ispec) / (r*r);
	}
	
	vec4 Ienv = getEnvironmentColor(R);
	vec4 Iref = getSpecularColor(fUV) * pow(max(dot(N, N), 0.0), shininess);
	Iref = clamp(Iref, 0.0, 1.0);
	finalColor += Ienv * Iref;

	gl_FragColor = finalColor * exposure; 
}
