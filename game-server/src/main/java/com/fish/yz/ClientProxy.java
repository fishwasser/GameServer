package com.fish.yz;

import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/12/9.
 *
 */
public class ClientProxy {
	private Channel channel;
	private Protocol.ClientInfo cinfo;
	private ByteString bciinfo;
	private ServerEntity master;

	public ClientProxy(Channel channel, Protocol.ClientInfo cinfo){
		this.channel = channel;
		this.cinfo = cinfo;
		this.bciinfo = cinfo.toByteString();
	}

	public Channel getChannel(){
		return channel;
	}

	public ServerEntity getMaster(){
		return this.master;
	}

	public void setMaster(ServerEntity master){
		this.master = master;
	}

	public void destroy(){
		channel = null;
		master = null;
	}

	public void createEntity(String entityName, ObjectId id){
		if(id == null){
			System.out.println("create entity need id");
			return;
		}
		Protocol.EntityInfo.Builder eib = Protocol.EntityInfo.newBuilder();
		eib.setRoutes(this.bciinfo);
		eib.setId(ByteString.copyFromUtf8(id.toString()));
		eib.setType(ByteString.copyFromUtf8(entityName));

        Protocol.Request.Builder rb = Protocol.Request.newBuilder();
        Protocol.GGMessage.Builder ggcb = Protocol.GGMessage.newBuilder();
        ggcb.setRoutes(bciinfo);
        ggcb.setType(Protocol.GGMessage.GGType.CreateEntity);
        ggcb.setParameters(eib.build().toByteString());
        rb.setCmdId(Protocol.Request.CmdIdType.GGMessage);
        rb.setExtension(Protocol.GGMessage.request, ggcb.build());
		this.channel.writeAndFlush(rb.build());
		System.out.println("createEntity " + rb.build());
	}

	public void destroyEntity(ObjectId id){
		if(id == null){
			System.out.println("create entity need id");
			return;
		}
		Protocol.EntityInfo.Builder eib = Protocol.EntityInfo.newBuilder();
		eib.setRoutes(this.bciinfo);
		eib.setId(ByteString.copyFromUtf8(id.toString()));

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		Protocol.GGMessage.Builder ggcb = Protocol.GGMessage.newBuilder();
		ggcb.setRoutes(bciinfo);
		ggcb.setType(Protocol.GGMessage.GGType.DestroyEntity);
		ggcb.setParameters(eib.build().toByteString());

		rb.setCmdId(Protocol.Request.CmdIdType.GGMessage);
		rb.setExtension(Protocol.GGMessage.request, ggcb.build());
		this.channel.writeAndFlush(rb.build());
		System.out.println("createEntity " + rb.build());
	}

	public void callClientMethod(String methodName, String parameters, ObjectId entityId){
	    Protocol.EntityMessage.Builder emb = Protocol.EntityMessage.newBuilder();
	    emb.setRoutes(this.bciinfo);
	    emb.setId(ByteString.copyFromUtf8(entityId.toString()));
	    emb.setMethod(ByteString.copyFromUtf8(methodName));
	    emb.setParameters(ByteString.copyFromUtf8(parameters));

        Protocol.Request.Builder rb = Protocol.Request.newBuilder();
        rb.setCmdId(Protocol.Request.CmdIdType.EntityMessage);
        rb.setExtension(Protocol.EntityMessage.request, emb.build());
        this.channel.writeAndFlush(rb.build());
	}

}
