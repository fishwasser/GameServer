package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.GameClientServiceHandler;
import com.google.protobuf.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/11/23.
 * 负责和games保持连接，并创建相应的clientproxy作为代理服务
 */
public class GameOiOClientsMgr {
	// 所有的服务器连接
	public Map<ServerInfoHolder, GameOiOClient> games = new HashMap<ServerInfoHolder, GameOiOClient>();
	// 失效的服务端连接
	public Map<ServerInfoHolder, GameOiOClient> lostGames = new HashMap<ServerInfoHolder, GameOiOClient>();

	private static GameOiOClientsMgr ins;

	private Bootstrap b = new Bootstrap();
	private EventLoopGroup group = new OioEventLoopGroup();
	private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public static GameOiOClientsMgr instance(){
		if (ins == null){
			ins = new GameOiOClientsMgr();
		}
		return ins;
	}

	private GameOiOClientsMgr() {
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

				pipeline.addLast(new GameClientServiceHandler());
			}
		});
	}

	public void setGameServerInfo(Protocol.GameServerInfos gsi){
		List<ServerInfoHolder> allHolders = new ArrayList<ServerInfoHolder>();
		for(int i = 0; i < gsi.getGameserversCount(); i++){
            Protocol.ServerInfo si = gsi.getGameservers(i);
            System.out.println("game system info, " + si + si.getIp().toStringUtf8() + " , " + si.getPort());
            ServerInfoHolder holder = new ServerInfoHolder(si);
            if (!games.containsKey(holder)){
            	if (!move(holder, lostGames, games)){
            		games.put(holder, new GameOiOClient(holder));
	            }
            }
			allHolders.add(holder);
        }
        for (ServerInfoHolder holder : games.keySet()){
			if (!allHolders.contains(holder)){
				move(holder, games, lostGames);
			}
        }

		this.connectGameServer();
	}

	private boolean move(ServerInfoHolder holder, Map<ServerInfoHolder, GameOiOClient> src, Map<ServerInfoHolder, GameOiOClient> dst){
		GameOiOClient client = src.get(holder);
		if (client != null){
			src.remove(holder);
			dst.put(holder, client);
			client.reset();
			return true;
		} else {
			return false;
		}
	}

	public void connectGameServer() {
		for (final Map.Entry<ServerInfoHolder, GameOiOClient> entry : games.entrySet()){
			// connect only when the client in not connected state
			if(entry.getValue().getState() != States.ST_NOT_CONNECTED)
				continue;
			connect(entry.getValue());
		}
	}

	public boolean checkAllConnected() {
		if (games.size() == 0)
			return false;
		for (GameOiOClient client : games.values()){
			if (!client.connected())
				return false;
		}

		return true;
	}

	public void connect(final GameOiOClient client){
		client.setState(States.ST_CONNECTING);
		final ChannelFuture cf = b.connect(client.holder.ip, client.holder.port);
		cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()){
					client.setChannelFuture(cf);
					client.setState(States.ST_CONNECTED);
				}else{
					client.setChannelFuture(null);
					client.setState(States.ST_NOT_CONNECTED);
					final EventLoop eventLoop = future.channel().eventLoop();
					eventLoop.schedule(new Runnable() {
						public void run() {
							GameOiOClientsMgr.this.connect(client);
						}
					}, 3L, TimeUnit.SECONDS);
				}
			}
		});
	}

	public GameOiOClient applyOneClient(){
		// 先随机给一个吧, 以后再改
		List<GameOiOClient> candidates = new ArrayList<GameOiOClient>();
		for (Map.Entry<ServerInfoHolder, GameOiOClient> entry : games.entrySet()){
			if (entry.getValue().getState() == States.ST_CONNECTED)
				candidates.add(entry.getValue());
		}
		if (candidates.size() > 0){
			int rndIdx = new Random().nextInt(candidates.size());
			return candidates.get(rndIdx);
		}
		return null;
	}

}
