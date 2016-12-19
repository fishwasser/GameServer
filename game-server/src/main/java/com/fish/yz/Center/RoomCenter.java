package com.fish.yz.Center;

import com.fish.yz.func.Room;
import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fishman on 18/12/2016.
 * 大厅
 */
public class RoomCenter extends Center {

    public Map<ObjectId, Room> rooms = new HashMap<ObjectId, Room>();


    public RoomCenter(){
        this(ObjectId.get());
    }

    public RoomCenter(ObjectId entityId){
        super(entityId);

        System.out.println("init room center");
    }

    public void startGame(ObjectId avatarId, ObjectId roomId){
        // todo: 检测 avatarId 是不是队长
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            if (room.getRemoteMB() == null) {
                Protocol.EntityMailbox cellMb = this.chooseOneCell();
                room.setRemoteMB(cellMb);
            }
            Protocol.EntityMailbox mb = room.getRemoteMB();
            this.callServerMethod(mb, this, "startGame", "", null);
        }
    }

    public List<ObjectId> getRoomMembers(ObjectId roomId){
        Room room = this.rooms.get(roomId);
        if (room != null){
            return room.members;
        }
        return null;
    }

    public void createRoom(ObjectId avatarId, ObjectId roomId){
        if (roomId == null){
            roomId = ObjectId.get();
        }
        this.rooms.put(roomId, new Room(roomId, avatarId));
    }

    public void joinRoom(ObjectId avatarId, ObjectId roomId){
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            room.join(avatarId);
        }
    }

    public void leftRoom(ObjectId avatarId, ObjectId roomId){
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            room.left(avatarId);
        }
    }

}
