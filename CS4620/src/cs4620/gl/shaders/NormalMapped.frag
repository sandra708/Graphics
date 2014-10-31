#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information
uniform float shininess;

varying vec4 worldPos;
varying vec3 fNormal;
varying vec2 fUV;

void main() {
	// TODO A4: Implement normal mapping fragment shader
	vec3 N = normalize(fNormal);
	vec3 V = normalize(worldCam - worldPos.xyz);
	
	vec3 R = V - (2 * dot(V, N) * N);
	
	Ienv = getEnvironmentColor(R);
	Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);
	Ispec = clamp(Ispec, 0.0, 1.0);

	gl_FragColor = Ienv * Ispec * exposure; 
}
