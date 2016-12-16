package com.fish.yz;


import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.DbServiceHandler;
import com.fish.yz.util.Config;
import com.google.protobuf.ExtensionRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Hello world!
 *
 */
public class DbServer {
	private String ip;
	private int port;
	private static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2;
	private static final int BIZTHREADSIZE = 10;
	private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public DbServer(){
		this.ip = Config.instance().get("dbserver.ip");
		this.port = Integer.parseInt(Config.instance().get("dbserver.port"));
	}

	// 开启服务
	public void startService() throws Exception {
		System.out.println("db service started");

		final EventLoopGroup bossGroup = new OioEventLoopGroup(BIZGROUPSIZE);
		final EventLoopGroup workerGroup = new OioEventLoopGroup(BIZTHREADSIZE);

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(OioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				Protocol.registerAllExtensions(registry);
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
				pipeline.addLast("protobufDecoder", new ProtobufDecoder(Protocol.Request.getDefaultInstance(), registry));
				pipeline.addLast("protobufEncoder", new ProtobufEncoder());

				pipeline.addLast(new DbServiceHandler());
			}
		});
		ChannelFuture f = bootstrap.bind(this.ip, this.port).sync();
		f.channel().closeFuture().sync();
		FMongoClients.instance().close();
	}

	public void checkGmConnected(){
		while (!GameManagerClient.instance().connected()){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("game manager server connected!");
	}

	public static void main(String[] args) throws Exception {System.out.println("start game server...");
		FMongoClients.instance().connectMongo();
		GameManagerClient.instance().connectGameManager();
		DbServer server = new DbServer();
		server.checkGmConnected();
		server.startService();
	}
}