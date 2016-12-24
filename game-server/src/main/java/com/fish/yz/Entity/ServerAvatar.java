package com.fish.yz.Entity;

import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xscn2426 on 2016/12/9.
 * 服务端玩家控制类
 */
public class ServerAvatar extends ServerEntity{
	public String account;
	public String password;
	public ObjectId teamId;

	public ServerAvatar(){
		this(ObjectId.get());
	}

	public ServerAvatar(ObjectId entityid){
		super(entityid);
		System.out.println("create avatar");
	}

	public void onBecomePlayer(){
		this.proxy.createEntity("ClientPlayer", this.id);
		this.proxy.callClientMethod("becomePlayer", "{}", this.id);
	}

	public void createTeam(){
		System.out.println("create team in avatar");

		Protocol.EntityMailbox centerMb = GameAPI.getGlobalEntityMailbox("RoomCenter");
		List<Object> list = new ArrayList<Object>();
		list.add(this.id);
		Document doc = new Document("p", list);
		this.callRoomCenterMethod("createRoom", doc);
	}

	public void onCreateTeam(Protocol.EntityMailbox emb, ObjectId teamId, boolean status){
		if (status){
			this.teamId = teamId;
			System.out.println("create team success");
			Document doc = new Document("ret", true).append("room_id", teamId.toString());
			this.proxy.callClientMethod("on_create_room", doc.toJson(), this.id);

			List<Object> list = new ArrayList<Object>();
			list.add(this.id);
			list.add(this.teamId);
			Document pdoc = new Document("p", list);
			this.callRoomCenterMethod("startGame", pdoc);
		} else {
			System.out.println("create team error");
			Document doc = new Document("ret", false).append("room_id", teamId.toString());
			this.proxy.callClientMethod("on_create_room", doc.toJson(), this.id);
		}
	}

	public void broadOp(Document doc){
		if (this.teamId == null)
			return;
		doc.append("roomId", this.teamId).append("avatarId", this.id);
		this.callRoomCenterMethod("broadOp", doc);
	}

	public void onUpdateOp(Protocol.EntityMailbox emb, ObjectId teamId){
		System.out.println("onUpdateOp in avatar" + teamId);
	}

	private void callRoomCenterMethod(String methodName, Document doc){
		Protocol.EntityMailbox centerMb = GameAPI.getGlobalEntityMailbox("RoomCenter");
		Protocol.EntityMailbox.Builder thisMb = Protocol.EntityMailbox.newBuilder();
		thisMb.setEntityid(this.getId());
		thisMb.setServerinfo(Repo.instance().serverInfo);

		if (centerMb != null){
			this.callServerMethod(centerMb, this, methodName, doc.toJson(), null);
		} else {
			System.out.println("not get the center!!");
		}
	}
}
