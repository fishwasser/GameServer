package com.fish.yz.Entity;

import com.fish.yz.CallBack;
import com.fish.yz.ClientProxy;
import com.fish.yz.GameManagerClient;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import org.bson.types.ObjectId;

import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/12/9.
 * 服务端Entity基类
 */
public class ServerEntity {
	protected ObjectId id;
	protected ClientProxy proxy;
	protected long saveTime = 15;


	public ServerEntity(){
		this(ObjectId.get());
	}

	public ServerEntity(ObjectId entityId){
		this.id = entityId;
		if (!EntityManager.hasEntity(entityId)){
			EntityManager.addEntity(entityId, this);
		}
	}

	public ByteString getId(){
		return ByteString.copyFromUtf8(id.toString());
	}

	public void setProxy(ClientProxy proxy){
		if (this.proxy != null){
			this.proxy.setMaster(null);
		}
		this.proxy = proxy;
		if (proxy != null) {
			proxy.setMaster(this);
			proxy.getChannel().eventLoop().scheduleAtFixedRate(new Runnable() {
				public void run() {
					ServerEntity.this.save();
				}
			}, saveTime, saveTime, TimeUnit.SECONDS);
		}
	}

	public void onBecomePlayer(){
		this.proxy.createEntity("ServerEntity", this.id);
		this.proxy.callClientMethod("becomePlayer", "{}", this.id);
	}

	public void giveClientTo(ServerEntity other){
		other.setProxy(this.proxy);
		other.onBecomePlayer();
		this.proxy = null;
	}

    public void callServerMethod(Protocol.EntityMailbox dstMb, ServerEntity entity, String methodName, String parameters, CallBack cb){
        GameManagerClient.instance().forwardEntityMessage(dstMb, entity, methodName, parameters, cb);
    }

	public void save(){
		System.out.println("i want to save but not do it");
	}

	public void destroy(){
		save();
		EntityManager.delEntity(this.id);
	}

	public void onLoseClient(){}
}
