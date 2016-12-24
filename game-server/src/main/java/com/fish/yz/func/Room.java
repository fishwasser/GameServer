package com.fish.yz.func;

import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fishman on 19/12/2016.
 * 房间
 */
public class Room {
	// 房间id
    public ObjectId id;
    // 房间里的所有队员，包括队长
    public Map<ObjectId, Protocol.EntityMailbox> members = new HashMap<ObjectId, Protocol.EntityMailbox>();
	// 房间关联的远端game
    public Protocol.EntityMailbox remoteMB;
    // 这个房间的队长
    public ObjectId leader;

    public Room(ObjectId roomId, ObjectId avatarId, Protocol.EntityMailbox emb){
        this.id = roomId;
        this.leader = avatarId;
        this.members.put(avatarId, emb);
    }

    public List<ObjectId> memberIds(){
    	List<ObjectId> ret = new ArrayList<ObjectId>();
    	ret.addAll(members.keySet());
    	return ret;
    }

	/**
	 * 更新队长
	 * @param avatarId
	 */
	public void updateLeader(ObjectId avatarId, Protocol.EntityMailbox emb){
	    if (this.members.containsKey(avatarId)){
	    	this.leader = avatarId;
	    } else {
	    	System.out.println("update leader not in member");
	    }
    }

	/**
	 * 加入房间
	 * @param avatarId
	 */
	public void join(ObjectId avatarId, Protocol.EntityMailbox emb){
        members.put(avatarId, emb);
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
}
