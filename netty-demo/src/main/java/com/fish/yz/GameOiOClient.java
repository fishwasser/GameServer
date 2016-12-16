package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.GameClientServiceHandler;
import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.bson.types.ObjectId;

/**
 * Created by xscn2426 on 2016/11/23.
 * Game的代理客户端
 */
public class GameOiOClient {
	public ChannelFuture cf = null;
	public ServerInfoHolder holder;
	private States state;


	public GameOiOClient(ServerInfoHolder holder) {
		this.holder = holder;
		this.state = States.ST_NOT_CONNECTED;
	}

	public void setChannelFuture(ChannelFuture cf){
		this.cf = cf;
		if (this.cf != null)
			this.cf.channel().attr(Repo.clientKey).set(this);
	}

	public States getState(){
		return this.state;
	}

	public void setState(States status){
		this.state = status;
	}

	public boolean connected(){
		return this.state == States.ST_CONNECTED;
	}

	public void reset(){
		this.state = States.ST_NOT_CONNECTED;
	}

	public void callServerInfo(Protocol.Request request){
		if (cf != null){
			System.out.println("write msg to game server " + request);
			cf.channel().writeAndFlush(request);
		}
	}

	// 在这里处理Proxy相关的任务
	public void connectServer(Protocol.ClientInfo ciInfo, Protocol.Request request){
		Protocol.Request r = request.toBuilder().setExtension(Protocol.ConnectServerRequest.request,
				request.toBuilder().getExtension(Protocol.ConnectServerRequest.request).
						toBuilder().setRoutes(ciInfo.toByteString()).build()).build();

		callServerInfo(r);
	}

	public void entityMessage(Protocol.ClientInfo ciInfo, Protocol.Request request){
		Protocol.Request r = request.toBuilder().setExtension(Protocol.EntityMessage.request,
				request.toBuilder().getExtension(Protocol.EntityMessage.request).
						toBuilder().setRoutes(ciInfo.toByteString()).build()).build();

		callServerInfo(r);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (getClass() != obj.getClass())
			return false;
		GameOiOClient o = (GameOiOClient)obj;
		return o.holder == this.holder;
	}

	@Override
	public int hashCode() {
		return this.holder.hashCode();
	}
}
