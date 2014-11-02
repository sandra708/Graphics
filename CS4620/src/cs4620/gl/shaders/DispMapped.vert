#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

// Shading Information
uniform float dispMagnitude;

varying vec4 worldPos;
varying vec3 fN;
varying vec2 fUV;

void main() {
	// TODO A4: Implement displacement mapping vertex shader
	//Calculate displacement
	//float phi = acos(vNormal.z);
	//float theta = atan(vNormal.y / vNormal.x);
	//vec2 nUV = vec2(phi, theta);
	vec4 dispC = getNormalColor(vUV);
	float disp = dispMagnitude * (dispC.x + dispC.y + dispC.z) / 3.0;
	vec3 dispV = vNormal * disp;
	// Calculate point in Object Space
	vec4 objPos = vPosition + vec4(dispV, 0.0);
	// Calculate Point In World Space
	worldPos = mWorld * (objPos);
	// Calculate Projected Point
	gl_Position = mViewProjection * worldPos;

	// We have to use the inverse transpose of the world transformation matrix for the normal
	fN = normalize((mWorldIT * vNormal).xyz);
	fUV = vUV;
}
