package com.fish.yz.service;

import com.fish.yz.GameOiOClientsMgr;
import com.fish.yz.GameServerFinder;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/12/3.
 * game finder 提供服务
 */
public class GameFinderServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
		System.out.println("GameFinderServiceHandler \n" + request);

		switch (request.getCmdId()){
			case FunctionalMessage:
				Protocol.FunctionalMessage fm = request.getExtension(Protocol.FunctionalMessage.request);
				if (fm.getFunc() == Protocol.FunctionalMessage.FuncType.SEND_GAMEINFO) {
					getGameServerInfo(channelHandlerContext, fm);
				}
				break;
		}
	}

	// 获取所有的game的信息
	public void getGameServerInfo(ChannelHandlerContext ctx, Protocol.FunctionalMessage fm) {
		System.out.println("getGameServerInfo \n" + fm);
		Protocol.GameServerInfos gsi = null;
		try {
			gsi = Protocol.GameServerInfos.parseFrom(fm.getParameters());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		GameOiOClientsMgr.instance().setGameServerInfo(gsi);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in game finder service " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 断线重连
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			public void run() {
				GameServerFinder.instance().start();
			}
		}, 3L, TimeUnit.SECONDS);
		super.channelInactive(ctx);
	}
}
