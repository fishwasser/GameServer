package com.fish.yz.service;

import com.fish.yz.FMongoClients;
import com.fish.yz.protobuf.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by xscn2426 on 2016/12/9.
 *
 */
public class DbServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Protocol.Request request) throws Exception {
		System.out.println("received from game");

		switch (request.getCmdId()){
			case DBMessage:
				Protocol.DBMessage req = request.getExtension(Protocol.DBMessage.request);
				FMongoClients.instance().handleOp(req, ctx);
				break;
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in db service " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
}