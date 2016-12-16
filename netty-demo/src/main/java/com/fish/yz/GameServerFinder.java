package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.GameFinderServiceHandler;
import com.fish.yz.util.Config;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/12/3.
 * 负责和gameserver通信并且处理games信息
 */
public class GameServerFinder {
    private Bootstrap b = new Bootstrap();
    private EventLoopGroup group = new OioEventLoopGroup();
    private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	private static GameServerFinder ins;
	private ChannelFuture cf = null;
	public States state = States.ST_NOT_CONNECTED;

	private String ip;
	private int port;

	public static GameServerFinder instance(){
		if (ins == null){
			ins = new GameServerFinder();
		}
		return ins;
	}

	private GameServerFinder(){
		this.ip = Config.instance().get("gm.ip");
		this.port = Integer.parseInt(Config.instance().get("gm.port"));

		Protocol.registerAllExtensions(registry);
		b.group(group);
		b.channel(OioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
				pipeline.addLast("protobufDecoder", new ProtobufDecoder(Protocol.Request.getDefaultInstance(), registry));
				pipeline.addLast("protobufEncoder", new ProtobufEncoder());

				pipeline.addLast(new GameFinderServiceHandler());
			}
		});
	}

	public void start(){
		this.state = States.ST_CONNECTING;
		this.cf = b.connect(ip, port);
		this.cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()){
					System.out.println("connect game manager success.");
                    GameServerFinder.instance().regGateServer();
					GameServerFinder.instance().getGameServersInfo();
					GameServerFinder.instance().state = States.ST_CONNECTED;
				}else{
					// 断线重连
					System.out.println("connect game manager error, reconnecting...");
					GameServerFinder.instance().cf.channel().close();
					GameServerFinder.instance().cf = null;
					GameServerFinder.instance().state = States.ST_NOT_CONNECTED;
					final EventLoop eventLoop = future.channel().eventLoop();
					eventLoop.schedule(new Runnable() {
						public void run() {
							GameServerFinder.instance().start();
						}
					}, 3L, TimeUnit.SECONDS);
				}
			}
		});
	}

	private void regGateServer(){
        Protocol.Request.Builder requestBuilder = Protocol.Request.newBuilder();
        requestBuilder.setCmdId(Protocol.Request.CmdIdType.FunctionalMessage);
        Protocol.FunctionalMessage.Builder fmBuilder = Protocol.FunctionalMessage.newBuilder();
        fmBuilder.setFunc(Protocol.FunctionalMessage.FuncType.REG_GATE);
        fmBuilder.setParameters(ByteString.copyFromUtf8(Config.instance().get("gate1.port")));

        requestBuilder.setExtension(Protocol.FunctionalMessage.request, fmBuilder.build());
        Protocol.Request request = requestBuilder.build();

        this.cf.channel().writeAndFlush(request);
	}

	private void getGameServersInfo(){
        Protocol.Request.Builder requestBuilder = Protocol.Request.newBuilder();
        requestBuilder.setCmdId(Protocol.Request.CmdIdType.FunctionalMessage);
        Protocol.FunctionalMessage.Builder fmBuilder = Protocol.FunctionalMessage.newBuilder();
        fmBuilder.setFunc(Protocol.FunctionalMessage.FuncType.SEND_GAMEINFO);

        requestBuilder.setExtension(Protocol.FunctionalMessage.request, fmBuilder.build());
        Protocol.Request request = requestBuilder.build();

		this.cf.channel().writeAndFlush(request);
	}

}
