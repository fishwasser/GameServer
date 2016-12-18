package com.fish.yz.Center;

import org.bson.types.ObjectId;

/**
 * Created by fishman on 18/12/2016.
 * 大厅
 */
public class RoomCenter extends Center {

    public RoomCenter(){
        this(ObjectId.get());
    }

    public RoomCenter(ObjectId entityId){
        super(entityId);

        System.out.println("init room center");
    }
}
