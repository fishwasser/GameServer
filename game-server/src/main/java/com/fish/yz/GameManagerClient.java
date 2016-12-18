package com.fish.yz;

import com.fish.yz.Entity.ServerEntity;
import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.GameManagerClientServiceHandler;
import com.fish.yz.util.Config;
import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.concurrent.TimeUnit;


/**
 * Created by xscn2426 on 2016/12/3.
 * 负责和game manager 通信
 */
public class GameManagerClient {
	private static GameManagerClient ins;

	private Bootstrap b = new Bootstrap();
	private EventLoopGroup group = new OioEventLoopGroup();
	private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public ChannelFuture cf = null;
	public States state = States.ST_NOT_CONNECTED;

	private String ip;
	private int port;

	public static GameManagerClient instance(){
		if (ins == null){
			ins = new GameManagerClient();
		}
		return ins;
	}

	private GameManagerClient() {
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

				pipeline.addLast(new GameManagerClientServiceHandler());
			}
		});
	}

	public void connectGameManager() {
		this.cf = b.connect(ip, port);
		this.state = States.ST_CONNECTING;
		this.cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()){
					GameManagerClient.this.state = States.ST_CONNECTED;
					regGameServer();
					System.out.println("connect game manager success " + ip + ".." + port);
				}else{
					GameManagerClient.this.state = States.ST_NOT_CONNECTED;
					final EventLoop loop = future.channel().eventLoop();
					loop.schedule(new Runnable() {
						public void run() {
							GameManagerClient.this.connectGameManager();
						}
					}, 3L, TimeUnit.SECONDS);
					System.out.println("reconnect game manager error");
				}
			}
		});
	}

	public boolean connected(){
		return this.state == States.ST_CONNECTED;
	}

	/////////////////// 对内提供的接口，可以访问gamemanager ///////////////////
	public void regGameServer(){
		Protocol.Request.Builder requestBuilder = Protocol.Request.newBuilder();
		requestBuilder.setCmdId(Protocol.Request.CmdIdType.FunctionalMessage);
		Protocol.FunctionalMessage.Builder fmBuilder = Protocol.FunctionalMessage.newBuilder();
		fmBuilder.setFunc(Protocol.FunctionalMessage.FuncType.REG_GAME);
		fmBuilder.setParameters(ByteString.copyFromUtf8(Repo.instance().port+""));

		requestBuilder.setExtension(Protocol.FunctionalMessage.request, fmBuilder.build());
		Protocol.Request request = requestBuilder.build();

		this.cf.channel().writeAndFlush(request);
	}

	public void regEntity(String entityUniqName, ServerEntity entity, CallBack callback){
		Protocol.GlobalEntityRegMsg.Builder gre = Protocol.GlobalEntityRegMsg.newBuilder();
		gre.setEntityUniqName(ByteString.copyFromUtf8(entityUniqName));
		Protocol.EntityMailbox.Builder mb = Protocol.EntityMailbox.newBuilder();
		mb.setEntityid(entity.getId());
		mb.setServerinfo(Repo.instance().serverInfo);
		if (callback != null)
			gre.setCallbackId(callback.getId());
		gre.setMailbox(mb);

		Protocol.FunctionalMessage.Builder fb = Protocol.FunctionalMessage.newBuilder();
		fb.setFunc(Protocol.FunctionalMessage.FuncType.REG_ENTITY);
		fb.setParameters(gre.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.FunctionalMessage);
		rb.setExtension(Protocol.FunctionalMessage.request, fb.build());

		this.cf.channel().writeAndFlush(rb.build());
	}

	public void unregEntity(String entityUniqName){
		Protocol.GlobalEntityRegMsg.Builder gre = Protocol.GlobalEntityRegMsg.newBuilder();
		gre.setEntityUniqName(ByteString.copyFromUtf8(entityUniqName));

		Protocol.FunctionalMessage.Builder fb = Protocol.FunctionalMessage.newBuilder();
		fb.setFunc(Protocol.FunctionalMessage.FuncType.UNREG_ENTITY);
		fb.setParameters(gre.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.FunctionalMessage);
		rb.setExtension(Protocol.FunctionalMessage.request, fb.build());

		this.cf.channel().writeAndFlush(rb.build());
	}

	public void forwardEntityMessage(Protocol.EntityMailbox dstMb, ServerEntity entity, String methodName, String parameters, CallBack cb){
		Protocol.ForwardMessageHeader.Builder fmsg = Protocol.ForwardMessageHeader.newBuilder();
		Protocol.EntityMailbox.Builder mb = Protocol.EntityMailbox.newBuilder();
		mb.setEntityid(entity.getId());
		mb.setServerinfo(Repo.instance().serverInfo);
		fmsg.setSrcmb(mb.build());
		fmsg.setDstmb(dstMb);
        if (cb != null)
            fmsg.setCallbackId(cb.getId());

		Protocol.EntityMessage.Builder emb = Protocol.EntityMessage.newBuilder();
		emb.setId(dstMb.getEntityid());
		emb.setMethod(ByteString.copyFromUtf8(methodName));
		if (null != parameters && !"".equals(parameters))
			emb.setParameters(ByteString.copyFromUtf8(parameters));
		emb.setRoutes(fmsg.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.EntityMessage);
		rb.setExtension(Protocol.EntityMessage.request, emb.build());

		this.cf.channel().writeAndFlush(rb.build());
	}
}