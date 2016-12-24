package com.fish.yz.Center;

import com.fish.yz.Repo;
import com.fish.yz.func.Room;
import com.fish.yz.protobuf.Protocol;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
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

    public void startGame(Protocol.EntityMailbox emb, ObjectId avatarId, ObjectId roomId){
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            if (!room.leader.equals(avatarId)){
	            System.out.println("only leader can start game! " + room.leader + " , " + avatarId);
	            return;
            }
            if (room.getRemoteMB() == null) {
                Protocol.EntityMailbox cellMb = this.chooseOneCell();
                room.setRemoteMB(cellMb);
            }
            Protocol.EntityMailbox mb = room.getRemoteMB();

	        List<Object> param = new ArrayList<Object>();
	        param.add(roomId);
	        param.add(room.memberIds());
	        Document doc = new Document("p", param);
            this.callServerMethod(mb, this, "startGame", doc.toJson(), null);
        }
    }

	public void broadOp(Protocol.EntityMailbox emb, Document doc){
    	ObjectId teamId = doc.getObjectId("roomId");
    	if (this.rooms.containsKey(teamId)){
		    Room room = this.rooms.get(teamId);
		    Protocol.EntityMailbox mb = room.getRemoteMB();

		    this.callServerMethod(mb, this, "broadOp", doc.toJson(), null);
	    }
	}

	public void onUpdateOp(Protocol.EntityMailbox emb, ObjectId teamId){
		System.out.println("onUpdateOp " + teamId);
		if (this.rooms.containsKey(teamId)){
			Room room = this.rooms.get(teamId);
			for (Map.Entry<ObjectId, Protocol.EntityMailbox> entry : room.members.entrySet()){
				Protocol.EntityMailbox mb = entry.getValue();
				List<Object> param = new ArrayList<Object>();
				param.add(teamId);
				Document d = new Document("p", param);
				this.callServerMethod(mb, this, "onUpdateOp", d.toJson(), null);
			}
		}
	}

    public List<ObjectId> getRoomMembers(Protocol.EntityMailbox emb, ObjectId roomId){
        Room room = this.rooms.get(roomId);
        if (room != null){
            return room.memberIds();
        }
        return null;
    }

    public void createRoom(Protocol.EntityMailbox emb, ObjectId avatarId, ObjectId roomId){
        if (roomId == null){
            roomId = ObjectId.get();
        }
        System.out.println("create team in center");
        this.rooms.put(roomId, new Room(roomId, avatarId, emb));

	    List<Object> list = new ArrayList<Object>();
	    list.add(roomId);
	    list.add(true);
	    Document doc = new Document("p", list);
        this.callBackMethod(emb, "onCreateTeam", doc);
    }

    public void joinRoom(Protocol.EntityMailbox emb, ObjectId avatarId, ObjectId roomId){
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            room.join(avatarId, emb);
        }
    }

    public void leftRoom(Protocol.EntityMailbox emb, ObjectId avatarId, ObjectId roomId){
        if (this.rooms.containsKey(roomId)){
            Room room = this.rooms.get(roomId);
            room.left(avatarId);
        }
    }

	private void callBackMethod(Protocol.EntityMailbox emb, String methodName, Document doc){
		Protocol.EntityMailbox.Builder thisMb = Protocol.EntityMailbox.newBuilder();
		thisMb.setEntityid(this.getId());
		thisMb.setServerinfo(Repo.instance().serverInfo);

		if (emb != null){
			this.callServerMethod(emb, this, methodName, doc.toJson(), null);
		} else {
			System.out.println("not get the center!!");
		}
	}

}
