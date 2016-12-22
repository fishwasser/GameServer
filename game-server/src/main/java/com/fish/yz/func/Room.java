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
	// 房间id
    public ObjectId id;
    // 房间里的所有队员，包括队长
    public List<ObjectId> members = new ArrayList<ObjectId>();
    // 房间关联的远端game
    public Protocol.EntityMailbox remoteMB;
    // 这个房间的队长
    public ObjectId leader;

    public Room(ObjectId roomId, ObjectId avatarId){
        this.id = roomId;
        this.leader = avatarId;
        this.members.add(avatarId);
    }

	/**
	 * 更新队长
	 * @param id
	 */
	public void updateLeader(ObjectId id){
	    if (this.members.contains(id)){
	    	this.leader = id;
	    } else {
	    	System.out.println("update leader not in member");
	    }
    }

	/**
	 * 加入房间
	 * @param id
	 */
	public void join(ObjectId id){
        members.add(id);
    }

	/**
	 * 离开房间
	 * @param id
	 */
	public void left(ObjectId id){
        members.remove(id);
    }

    public Protocol.EntityMailbox getRemoteMB(){
        return remoteMB;
    }

    public void setRemoteMB(Protocol.EntityMailbox mb){
        this.remoteMB = mb;
    }

	public ObjectId getLeader() {
		return leader;
	}

	public void setLeader(ObjectId leader) {
		this.leader = leader;
	}
}
