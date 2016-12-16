package com.fish.yz.Entity;

import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/12/9.
 * 游戏启动负责配置、启动组件的功能
 */
public class ServerStart extends ServerEntity{
	public ServerStart(){
		this(ObjectId.get());
	}

	public ServerStart(ObjectId entityId){
		super(entityId);

	}

}
