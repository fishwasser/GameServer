package com.fish.yz.func;

import com.fish.yz.info.Unit;
import com.fish.yz.util.Quaternion;
import com.fish.yz.util.Vector3;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xscn2426 on 2016/12/21.
 * 一个组，战斗的最小单位
 * 战斗过程中不能随意加入队伍，队伍不死不休
 */
public class Team {
	public ObjectId id;
	// 队员
	public Map<ObjectId, Unit> units = new HashMap<ObjectId, Unit>();

	public Team(){
		this(ObjectId.get());
	}

	public Team(ObjectId id){
		this.id = id;
	}

	public void initTeam(List<ObjectId> members){
		for (ObjectId id : members){
			this.initUnit(id, Vector3.zero(), new Quaternion(0,0,0,1), new Vector3(0,0,1));
		}
	}

	public void initUnit(ObjectId id, Vector3 pos, Quaternion rot, Vector3 forward){
		if (this.units.containsKey(id)){
			this.units.get(id).updateStates(pos, rot, forward);
		} else {
			Unit unit = new Unit(id);
			unit.updateStates(pos, rot, forward);
			this.units.put(id, unit);
		}
	}

	/**
	 * 更新队伍中的人的状态
	 * @param id
	 * @param seq
	 */
	public void updateOp(ObjectId id, int seq){
		System.out.println("update one unit's state");
	}

	public void tick(){

	}

}
