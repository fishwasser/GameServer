package com.fish.yz.service;

import com.fish.yz.GameOiOClient;
import com.fish.yz.ProxyManager;
import com.fish.yz.Repo;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

import com.fish.yz.protobuf.Protocol;


public class GateServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
        System.out.println("received from client");

        switch (request.getCmdId()){
			case ConnectServerRequest:
                connectServer(channelHandlerContext, request);
                break;
			case EntityMessage:
                entityMessage(channelHandlerContext, request);
                break;
        }
    }

    public void connectServer(ChannelHandlerContext ctx, Protocol.Request request){
	    System.out.println("client connect server" + request);
	    Protocol.ConnectServerRequest cs = request.getExtension(Protocol.ConnectServerRequest.request);

	    // 更新id对应channel关系
	    String deviceID = cs.getDeviceid().toStringUtf8();
	    Channel oldChannel = ProxyManager.instance().getChannelByClientId(deviceID);
	    if (oldChannel != null){
	    	if (oldChannel.isActive())
		        oldChannel.close();
		    ProxyManager.instance().removeChannelByClientId((deviceID));
	    }
	    ProxyManager.instance().setChannelClientId(deviceID, ctx.channel());

	    // 更新id对应game关系
	    GameOiOClient gameClient = ProxyManager.instance().getGameByClientId(deviceID);
		if (gameClient == null){
			gameClient = ProxyManager.instance().applyGameClient(deviceID);
			ProxyManager.instance().setGameClientId(deviceID, gameClient);
		}

	    String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getHostName();
	    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();

	    Protocol.ClientInfo.Builder ciBuilder = Protocol.ClientInfo.newBuilder();
	    ciBuilder.setIp(ByteString.copyFromUtf8(ip))
			    .setPort(port)
			    .setClientid(cs.getDeviceid())
			    .setSessionid(cs.getDeviceid())
			    .setGateid(ByteString.copyFromUtf8("gate1"));
		Protocol.ClientInfo ciInfo = ciBuilder.build();
	    // 注册信息 client info
	    ctx.channel().attr(Repo.clientInfoKey).set(ciInfo);
	    gameClient.connectServer(ciInfo, request);
    }

    public void entityMessage(ChannelHandlerContext ctx, Protocol.Request request){
	    Protocol.ClientInfo ciInfo = ctx.channel().attr(Repo.clientInfoKey).get();
	    GameOiOClient gameClient = ProxyManager.instance().getGameByClientId(ciInfo.getClientid().toStringUtf8());
	    gameClient.entityMessage(ciInfo, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    System.out.println("exceptionCaught in gate service " + cause.getMessage());
    }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive " + ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
}