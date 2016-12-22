package com.fish.yz.Center;

import com.fish.yz.func.Team;
import com.fish.yz.protobuf.Protocol;
import org.bson.types.ObjectId;

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
    }

	public void tick(){
    	for (Team team : this.teams.values()){
    		team.tick();
	    }
	}
}
