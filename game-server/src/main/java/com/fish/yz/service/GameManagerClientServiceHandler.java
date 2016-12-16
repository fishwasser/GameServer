package com.fish.yz.service;

import com.fish.yz.GameManagerClient;
import com.fish.yz.Repo;
import com.fish.yz.ServerInfoHolder;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;

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
					case FORWARD_ENTITY_MESSAGE:
						forwardEntityMessage(channelHandlerContext, fm);
						break;
					case GMRETURNVAL:
						gmReturn(channelHandlerContext, fm);
						break;
				}
				break;
		}
	}

	public void regEntity(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
		System.out.println("reg entity todo but not do");
	}

	public void unregEntity(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
		System.out.println("unreg entity todo but not do");
	}

	public void forwardEntityMessage(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
		System.out.println("forward entity message todo but not do");
	}

	public void gmReturn(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) throws InvalidProtocolBufferException {
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