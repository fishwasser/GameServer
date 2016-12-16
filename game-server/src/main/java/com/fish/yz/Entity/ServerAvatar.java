package com.fish.yz.Entity;

import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/12/9.
 * 服务端玩家控制类
 */
public class ServerAvatar extends ServerEntity{
	public String account;
	public String password;


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

}
