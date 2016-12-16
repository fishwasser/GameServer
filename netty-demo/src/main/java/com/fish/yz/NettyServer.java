package com.fish.yz;

import com.fish.yz.service.GateServiceHandler;
import com.fish.yz.util.Config;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import com.google.protobuf.ExtensionRegistry;

import com.fish.yz.protobuf.*;
import org.bson.types.ObjectId;

import java.nio.ByteOrder;


public class NettyServer {
    private String ip;
    private int port;
    private static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2;
    private static final int BIZTHREADSIZE = 10;
	private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public NettyServer(){
		this.ip = Config.instance().get("gate1.ip");
		this.port = Integer.parseInt(Config.instance().get("gate1.port"));
	}

    // 开启服务
    public void startService() throws Exception {
	    System.out.println("start gate service...");

	    final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
	    final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);

	    ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
	    bootstrap.childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                Protocol.registerAllExtensions(registry);
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("protobufDecoder", new ProtobufDecoder(Protocol.Request.getDefaultInstance(), registry));
                pipeline.addLast("protobufEncoder", new ProtobufEncoder());

                pipeline.addLast(new GateServiceHandler());
            }
        });
        ChannelFuture f = bootstrap.bind(this.ip, this.port).sync();

	    f.channel().closeFuture().sync();
        System.out.println("gate service started");
    }

    public void connectGameServer(){
	    GameServerFinder.instance().start();
    }

    public void checkGameServerConnected(){
	    while (!GameOiOClientsMgr.instance().checkAllConnected()){
		    try {
			    Thread.sleep(100);
		    } catch (InterruptedException e) {
			    e.printStackTrace();
		    }
	    }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("start gate server...");
	    NettyServer server = new NettyServer();
		server.connectGameServer();
		server.checkGameServerConnected();
	    server.startService();
    }
}