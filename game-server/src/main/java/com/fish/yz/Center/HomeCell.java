package com.fish.yz.Center;

import com.fish.yz.Repo;
import com.fish.yz.func.Room;
import com.fish.yz.func.Team;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fishman on 18/12/2016.
 * 小房间组
 */
public class HomeCell extends Cell{

    public Map<ObjectId, Team> teams = new HashMap<ObjectId, Team>();

    public HomeCell(){
        this(ObjectId.get());
    }

    public HomeCell(ObjectId entityId){
        super(entityId);
        this.setCenterName("RoomCenter");

        System.out.println("init home cell");
    }

	/**
	 * 开启一场战斗
	 * roomId与teamId一一对应
	 */
	public void startGame(Protocol.EntityMailbox emb, ObjectId roomId, List<ObjectId> members){
		System.out.println("home cell start a game " + roomId);
		if (this.teams.containsKey(roomId)){
			System.out.println("this team already exist");
			return;
		}
		Team team = new Team(roomId);
		team.initTeam(members);
		this.teams.put(roomId, team);
    }

	public void tick(){
    	for (Team team : this.teams.values()){
    		team.tick();
	    }
	}

	public void broadOp(Protocol.EntityMailbox emb, Document doc){
		ObjectId teamId = doc.getObjectId("roomId");
		ObjectId avatarId = doc.getObjectId("avatarId");

		System.out.println("broadOp in cell " + teamId + " , " + avatarId);

		if (this.teams.containsKey(teamId)) {
			Team team = this.teams.get(teamId);
			team.updateOp(emb, doc);

			System.out.println("onUpdateOp in cell");
			List<Object> param = new ArrayList<Object>();
			param.add(teamId);
			Document d = new Document("p", param);
			this.callRoomCenterMethod("onUpdateOp", d);
		}
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
