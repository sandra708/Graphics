#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

//Object Information
uniform mat4 mWorld;

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

void main() {
	// TODO A4: Implement normal mapping fragment shader
	//Calculate center of sphere
	vec4 objCenter = vec4(0.0, 0.0, 0.0, 1.0);
	vec4 sphereCenter = mWorld * objCenter;
	
	vec3 N = normalize((worldPos - sphereCenter).xyz);
	float phi = acos(N.z);
	float theta = atan(N.y / N.x);
	vec2 UV = vec2(phi, theta);
	vec4 Inorm = getNormalColor(UV);
	clamp(Inorm, 0.0, 1.0);
	
	gl_FragColor = Inorm * exposure;
}
