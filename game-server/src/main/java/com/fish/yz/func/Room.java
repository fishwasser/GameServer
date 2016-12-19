package com.fish.yz.func;

import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishman on 19/12/2016.
 * 房间
 */
public class Room {
    public ObjectId id;
    public List<ObjectId> members = new ArrayList<ObjectId>();
    public Protocol.EntityMailbox remoteMB;

    public Room(ObjectId roomId, ObjectId avatarId){
        this.id = roomId;
        this.members.add(avatarId);
    }

    public void join(ObjectId id){
        members.add(id);
    }

    public void left(ObjectId id){
        members.remove(id);
    }

    public Protocol.EntityMailbox getRemoteMB(){
        return remoteMB;
    }

    public void setRemoteMB(Protocol.EntityMailbox mb){
        this.remoteMB = mb;
    }


}
