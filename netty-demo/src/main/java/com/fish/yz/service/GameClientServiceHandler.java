package com.fish.yz.service;

import com.fish.yz.GameOiOClient;
import com.fish.yz.GameOiOClientsMgr;
import com.fish.yz.ProxyManager;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/11/23.
 * 处理game转发过来的请求
 */
public class GameClientServiceHandler extends SimpleChannelInboundHandler<Protocol.Request>{

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
		System.out.println("in GameClientServiceHandler \n" + request);

		switch (request.getCmdId()){
			case ConnectServerReply:
				connectServerReply(channelHandlerContext, request);
				break;
			case EntityMessage:
				entityMessage(channelHandlerContext, request);
				break;
            case GGMessage:
                dealGGMessage(channelHandlerContext, request);
                break;
		}
	}

	public void connectServerReply(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
		System.out.println("client connect server" + request);

		Protocol.ConnectServerReply cs = request.getExtension(Protocol.ConnectServerReply.request);
		Protocol.ClientInfo info = Protocol.ClientInfo.parseFrom(cs.getRoutes());
		Channel channel = ProxyManager.instance().getChannelByClientId(info.getClientid().toStringUtf8());
		if (channel == null)
			return;

		channel.writeAndFlush(request);
	}

	public void entityMessage(ChannelHandlerContext ctx, final Protocol.Request request) throws InvalidProtocolBufferException {
        Protocol.EntityMessage cs = request.getExtension(Protocol.EntityMessage.request);
        Protocol.ClientInfo info = Protocol.ClientInfo.parseFrom(cs.getRoutes());
        final Channel channel = ProxyManager.instance().getChannelByClientId(info.getClientid().toStringUtf8());
        if (channel == null) {
	        System.out.println("not find channel for entityMessage " + info.getClientid().toStringUtf8());
            return;
        }

		channel.writeAndFlush(request);
	}

	public void dealGGMessage(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
        Protocol.GGMessage cs = request.getExtension(Protocol.GGMessage.request);
        Protocol.ClientInfo info = Protocol.ClientInfo.parseFrom(cs.getRoutes());
        Channel channel = ProxyManager.instance().getChannelByClientId(info.getClientid().toStringUtf8());
        if (channel == null) {
            System.out.println("not find channel for dealGGMessage " + info.getClientid().toStringUtf8());
            return;
        }

        channel.writeAndFlush(request);
    }

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in game client " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		GameOiOClient client = ctx.channel().attr(Repo.clientKey).get();
		client.reset();
		GameOiOClientsMgr.instance().connect(client);
	}
}
