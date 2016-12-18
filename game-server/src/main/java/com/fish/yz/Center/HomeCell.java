package com.fish.yz.Center;

import org.bson.types.ObjectId;

/**
 * Created by fishman on 18/12/2016.
 * 小房间组
 */
public class HomeCell extends Cell{

    public HomeCell(){
        this(ObjectId.get());
    }

    public HomeCell(ObjectId entityId){
        super(entityId);
        this.setCenterName("RoomCenter");

        System.out.println("init home cell");
    }
}
