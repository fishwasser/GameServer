package com.fish.yz.util;

/**
 * Created by fishman on 19/12/2016.
 * 四元数，用于游戏中的旋转, 暂时提供求共轭、乘法、绕y轴旋转的功能
 */
public class Quaternion {
    public float x,y,z, w;

    public Quaternion(){
	    this.x = 0;
	    this.y = 0;
	    this.z = 0;
	    this.w = 0;
    }

    public Quaternion(float x, float y, float z, float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

	public void normalizeTo(){
		float length = (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
		if (length != 0){
			this.x /= length;
			this.y /= length;
			this.z /= length;
			this.w /= length;
		}
	}

	public Quaternion conjugate(){
		return new Quaternion(-this.x, -this.y, -this.z, this.w);
	}

	public Quaternion eulerY(float angle){
		angle = (float) (angle / 180.0 * Math.PI);
		float sinHalfAngle = (float) Math.sin(angle / 2);
		float cosHalfAngle = (float) Math.cos(angle / 2);
		float rx = 0;
		float ry = sinHalfAngle;
		float rz = 0;
		float rw = cosHalfAngle;
		return new Quaternion(rx, ry, rz, rw);
	}

	public Quaternion mul(Quaternion other){
		float w = (this.w * other.w) - (this.x * other.x) - (this.y * other.y) - (this.z * other.z);
		float x = (this.x * other.w) + (this.w * other.x) + (this.y * other.z) - (this.z * other.y);
		float y = (this.y * other.w) + (this.w * other.y) + (this.z * other.x) - (this.x * other.z);
		float z = (this.z * other.w) + (this.w * other.z) + (this.x * other.y) - (this.y * other.x);

		return new Quaternion(x, y, z, w);
	}

	public Quaternion mulTo(Quaternion other){
		float w = (this.w * other.w) - (this.x * other.x) - (this.y * other.y) - (this.z * other.z);
		float x = (this.x * other.w) + (this.w * other.x) + (this.y * other.z) - (this.z * other.y);
		float y = (this.y * other.w) + (this.w * other.y) + (this.z * other.x) - (this.x * other.z);
		float z = (this.z * other.w) + (this.w * other.z) + (this.x * other.y) - (this.y * other.x);

		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
}
