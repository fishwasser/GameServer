package com.fish.yz.info;

import org.bson.types.ObjectId;

/**
 * Created by fishman on 19/12/2016.
 *
 */
public class Unit {
    public ObjectId id;
    public Vector3 forward;
    public Vector3 pos;
    public Quaternion rot;
    public int seq;
    public float moveSpeed;
    public float turnSpeed;

    public Unit(){}

    public void updateStates(Vector3 pos, Quaternion rot, Vector3 forward){
        this.pos = pos;
        this.forward = forward;
        this.rot = rot;
    }

    public void getStates(){

    }

    public void initLoad(){
        this.moveSpeed = 5.0f;
        this.turnSpeed = 5.0f;
    }
}
