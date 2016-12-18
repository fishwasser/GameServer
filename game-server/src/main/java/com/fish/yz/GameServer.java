package com.fish.yz;

import com.fish.yz.Entity.ServerStart;
import com.fish.yz.protobuf.Protocol;

import com.fish.yz.service.GameServiceHandler;
import com.fish.yz.util.Config;
import com.google.protobuf.ByteString;
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
public class GameServer {
    private static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int BIZTHREADSIZE = 10;
    private static ExtensionRegistry registry = ExtensionRegistry.newInstance();
    private static int game;

    public GameServer(){
        String tmp = "game" + game;
        Repo.instance().ip = Config.instance().get(tmp + ".ip");
        Repo.instance().port = Integer.parseInt(Config.instance().get(tmp + ".port"));
	    Protocol.ServerInfo.Builder sb = Protocol.ServerInfo.newBuilder();
	    sb.setIp(ByteString.copyFromUtf8(Repo.instance().ip));
	    sb.setPort(Repo.instance().port);
	    Repo.instance().serverInfo = sb.build();
    }

    // 开启服务
    public void startService() throws Exception {
	    System.out.println("game service started");
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

                pipeline.addLast(new GameServiceHandler());
            }
        });
        ChannelFuture f = bootstrap.bind(Repo.instance().ip, Repo.instance().port).sync();
        f.channel().closeFuture().sync();
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

	public void checkDbConnected(){
		while (!DbClient.instance().connected()){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("db server connected!");
	}

	public void start(ServerStart start){
        start.startGame();
    }

    public void checkStartSuccess(ServerStart start){
        while (!start.hasStarted()){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("start server success!!!");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("start game server...");
        if (args.length < 1){
            System.out.println("start game server error without setting args");
        }
        game = Integer.parseInt(args[0]);

        ServerStart start = new ServerStart("game"+args[0]);
        GameServer server = new GameServer();
        GameManagerClient.instance().connectGameManager();
        server.checkGmConnected();
        DbClient.instance().connectDbServer();
        server.checkDbConnected();
        server.start(start);
        server.checkStartSuccess(start);
        server.startService();
    }
}