package com.fish.yz.util;


/**
 * Created by fishman on 19/12/2016.
 *
 */
public class Vector3 {
    public float x,y,z;

    public Vector3(){}

    public Vector3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void load(Vector3 v){
    	this.x = v.x;
    	this.y = v.y;
    	this.z = v.z;
    }

    public void setZero(){
	    this.x = 0;
	    this.y = 0;
	    this.z = 0;
    }

    public static Vector3 zero(){
    	return new Vector3(0,0,0);
    }

    public Vector3 neg(){
    	return new Vector3(-this.x, -this.y, -this.z);
    }

	public Vector3 add(Vector3 other){
		return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public Vector3 sub(Vector3 other){
		return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
	}

	public Vector3 mul(float a){
		return new Vector3(this.x * a, this.y * a, this.z * a);
	}

	public Vector3 div(float a){
		if (a != 0)
			return new Vector3(this.x / a, this.y / a, this.z / a);
		else
			return null;
	}

	public Vector3 addTo(Vector3 other){
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public Vector3 subTo(Vector3 other){
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}

	public Vector3 mulTo(float a){
		this.x *= a;
		this.y *= a;
		this.z *= a;
		return this;
	}

	public Vector3 divTo(float a){
		if (a != 0){
			this.x /= a;
			this.y /= a;
			this.z /= a;
		}
		return this;
	}

	public Vector3 normalize(){
		float magSq = this.x * this.x + this.y * this.y + this.z * this.z;
		if(magSq > 0){
			float oneOverMag = (float)(1.0 / Math.sqrt(magSq));
			return new Vector3(this.x * oneOverMag, this.y * oneOverMag, this.z * oneOverMag);
		}
		return null;
	}

	public Vector3 normalizeTo(){
		float magSq = this.x * this.x + this.y * this.y + this.z * this.z;
		if(magSq > 0){
			double oneOverMag = 1.0 / Math.sqrt(magSq);
			this.x *= oneOverMag;
			this.y *= oneOverMag;
			this.z *= oneOverMag;
		}
		return this;
	}

	public float magnitude(){
		return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public float magnitudeSqr(){
		return (float)(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	@Override
	public String toString() {
		return "{x:" + this.x+ ",y:" + this.y + ",z:" + this.z+"}";
	}

	public String toBson() {
		return "[" + this.x+ "," + this.y + "," + this.z+"]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Vector3 o = (Vector3)obj;
		return o.x == this.x && o.y == this.y && o.z == this.z;
	}

	@Override
	public int hashCode() {
		return (int)this.x ^ (int)this.y ^ (int)this.z;
	}
}
