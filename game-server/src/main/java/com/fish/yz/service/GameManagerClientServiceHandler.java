package com.fish.yz.service;

import com.fish.yz.*;
import com.fish.yz.Entity.EntityManager;
import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.util.GameAPI;
import com.fish.yz.util.ReflectUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/12/3.
 *
 */
public class GameManagerClientServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
		System.out.println("received from game manager, " + request);

		switch (request.getCmdId()){
			case EntityMessage:
                forwardEntityMessage(channelHandlerContext, request);
				break;
			case FunctionalMessage:
				Protocol.FunctionalMessage fm = request.getExtension(Protocol.FunctionalMessage.request);
				switch (fm.getFunc()){
					case SEND_GAMEINFO:
						getGameServerInfo(channelHandlerContext, fm);
						break;
					case REG_ENTITY:
						regEntity(channelHandlerContext, fm);
						break;
					case UNREG_ENTITY:
						unregEntity(channelHandlerContext, fm);
					case GMRETURNVAL:
						gmReturn(channelHandlerContext, request);
						break;
				}
				break;
		}
	}

	public void regEntity(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
		Protocol.GlobalEntityRegMsg msg = Protocol.GlobalEntityRegMsg.parseFrom(fm.getParameters());
        System.out.println("reg entity todo but not do " + msg);
		Repo.instance().entities.put(msg.getEntityUniqName().toStringUtf8(), msg.getMailbox());
	}

	public void unregEntity(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
		Protocol.GlobalEntityRegMsg msg = Protocol.GlobalEntityRegMsg.parseFrom(fm.getParameters());
        System.out.println("unreg entity todo but not do " + msg);
		Repo.instance().entities.remove(msg.getEntityUniqName().toStringUtf8());
	}

    public void forwardEntityMessage(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
        System.out.println("forward entity message " + request);
        Protocol.EntityMessage em = request.getExtension(Protocol.EntityMessage.request);
        Protocol.ForwardMessageHeader msg = Protocol.ForwardMessageHeader.parseFrom(em.getRoutes());
        Protocol.EntityMailbox srcMb = msg.getSrcmb();
        ObjectId id = new ObjectId(em.getId().toStringUtf8());
        ServerEntity entity = EntityManager.getEntity(id);
        String methodName = em.getMethod().toStringUtf8();
        if (entity == null) {
            System.out.println("call entity message not has entity " + id);
            return;
        }
        Method method = GameAPI.getDeclaredMethod(entity, methodName, Protocol.EntityMailbox.class, Document.class);
	    Document doc = Document.parse(em.getParameters().toStringUtf8());
        if (method != null){
            try {
                method.invoke(entity, srcMb, doc);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

	public void gmReturn(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
        Protocol.FunctionalMessage fm = request.getExtension(Protocol.FunctionalMessage.request);
        Protocol.GmReturnVal val = Protocol.GmReturnVal.parseFrom(fm.getParameters());
        ByteString id = val.getCallbackId();
        if (Repo.instance().callbacks.containsKey(id)){
            CallBack cb = Repo.instance().callbacks.get(id);
            cb.onResult(request);
        }
	    System.out.println("gm return todo but not do");
	}

	// 获取所有的game的信息, 暂时还没指定有什么用呢
	public void getGameServerInfo(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) {
		Protocol.GameServerInfos gsi = null;
		try {
			gsi = Protocol.GameServerInfos.parseFrom(fm.getParameters());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < gsi.getGameserversCount(); i++) {
			Protocol.ServerInfo si = gsi.getGameservers(i);
			System.out.println("system info, " + si + si.getIp().toStringUtf8() + " " + si.getPort());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in gm service " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 断线重连
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			public void run() {
				GameManagerClient.instance().connectGameManager();
			}
		}, 3L, TimeUnit.SECONDS);
		super.channelInactive(ctx);
	}
}