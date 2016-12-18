package com.fish.yz.service;

import com.fish.yz.ClientProxy;
import com.fish.yz.Entity.EntityManager;
import com.fish.yz.Repo;
import com.fish.yz.Entity.EntityFactory;
import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by xscn2426 on 2016/11/23.
 * game提供的服务
 */
public class GameServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
		System.out.println("received from gate " + request);

		switch (request.getCmdId()){
			case ConnectServerRequest:
				connectServer(channelHandlerContext, request);
				break;
			case EntityMessage:
				entityMessage(channelHandlerContext, request);
				break;
		}
	}

	public void connectServer(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
		System.out.println("client connect server \n" + request);

		Protocol.ConnectServerRequest css = request.getExtension(Protocol.ConnectServerRequest.request);
		Protocol.ClientInfo info = Protocol.ClientInfo.parseFrom(css.getRoutes());
		switch (css.getType()){
			case NEW_CONNECTION:
				dealNewConnection(info, ctx);
				break;
		}
	}

	private void dealNewConnection(Protocol.ClientInfo info, ChannelHandlerContext ctx){
		String clientId = info.getClientid().toStringUtf8();
		ClientProxy proxy = Repo.instance().clients.get(clientId);
		if (proxy != null){
			if (proxy.getMaster() != null)
				proxy.getMaster().onLoseClient();
			proxy.destroy();
		}
		proxy = new ClientProxy(ctx.channel(), info);
		Repo.instance().clients.put(clientId, proxy);
		ServerEntity entity = EntityFactory.createEntity("ServerBoot");
		entity.setProxy(proxy);
		entity.onBecomePlayer();
	}

	public void entityMessage(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
		Protocol.EntityMessage em = request.getExtension(Protocol.EntityMessage.request);
        Protocol.ClientInfo info = Protocol.ClientInfo.parseFrom(em.getRoutes());
		ObjectId id = new ObjectId(em.getId().toStringUtf8());
        callEntityMethod(info, id, em);
	}

	private void callEntityMethod(Protocol.ClientInfo info, ObjectId id, Protocol.EntityMessage entitymsg){
        String clientId = info.getClientid().toStringUtf8();
        ClientProxy proxy = Repo.instance().clients.get(clientId);
        if (proxy == null){
            System.out.println("call entity message not has id " + id);
        } else {
            String methodName = entitymsg.getMethod().toStringUtf8();
            ServerEntity entity = EntityManager.getEntity(id);
            if (entity == null){
                System.out.println("call entity message not has entity " + id);
                return;
            }
			Method method = GameAPI.getDeclaredMethod(entity, methodName, Document.class);
			Document doc = Document.parse(entitymsg.getParameters().toStringUtf8());
			if (method != null){
				try {
					method.invoke(entity, doc);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}

            /*Class cls = entity.getClass();
            Document doc = Document.parse(entitymsg.getParameters().toStringUtf8());
            try {
                Method method = cls.getDeclaredMethod(methodName, Document.class);
                if (method == null){
                    System.out.println("call entity message not find method");
                    return;
                }
                method.invoke(entity, doc);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in game service " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
}