package com.gamesparks.sdk.realtime;

public class RTVector {
	public Float x = null;
	public Float y = null;
	public Float z = null;
	public Float w = null;
	
	public RTVector() {
	}
	
	public RTVector(float x, float y) {
		this.x = new Float(x);
		this.y = new Float(y);
	}
	
	public RTVector(float x, float y, float z) {
		this.x = new Float(x);
		this.y = new Float(y);
		this.z = new Float(z);
	}
	
	public RTVector(float x, float y, float z, float w) {
		this.x = new Float(x);
		this.y = new Float(y);
		this.z = new Float(z);
		this.w = new Float(w);
	}
}
