package com.fish.yz;

import com.fish.yz.protobuf.Protocol;
import com.fish.yz.service.DbClientServiceHandler;
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
import org.bson.types.ObjectId;

import java.util.concurrent.TimeUnit;


/**
 * Created by xscn2426 on 2016/12/3.
 * 负责和game manager 通信
 */
public class DbClient {
	private static DbClient ins;

	private Bootstrap b = new Bootstrap();
	private EventLoopGroup group = new OioEventLoopGroup();
	private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public ChannelFuture cf = null;
	public States state = States.ST_NOT_CONNECTED;

	private String ip;
	private int port;

	public static DbClient instance(){
		if (ins == null){
			ins = new DbClient();
		}
		return ins;
	}

	private DbClient() {
		this.ip = Config.instance().get("dbserver.ip");
		this.port = Integer.parseInt(Config.instance().get("dbserver.port"));

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

				pipeline.addLast(new DbClientServiceHandler());
			}
		});
	}

	public void connectDbServer() {
		this.state = States.ST_CONNECTING;
		this.cf = b.connect(ip, port);
		this.cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()){
					DbClient.this.state = States.ST_CONNECTED;
					System.out.println("connect db server success");
				}else{
					DbClient.this.state = States.ST_NOT_CONNECTED;
					System.out.println("reconnect db server error");
					// 失败重连
					final EventLoop loop = future.channel().eventLoop();
					loop.schedule(new Runnable() {
						public void run() {
							DbClient.this.connectDbServer();
						}
					}, 3L, TimeUnit.SECONDS);
				}
			}
		});
	}

	public boolean connected(){
		return this.state == States.ST_CONNECTED;
	}

	///////////////////////////////////提供对外的方法///////////////////////////
	public void find(String collectionName, String query, String fields, CallBack callback){
		Protocol.FindDocRequest.Builder ccb = Protocol.FindDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));
		ccb.setQuery(ByteString.copyFromUtf8(query));

		if (fields != null && !"".equals(fields))
			ccb.setFields(ByteString.copyFromUtf8(fields));
		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.FindDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void count(String collectionName, String query, CallBack callback){
		Protocol.CountDocRequest.Builder ccb = Protocol.CountDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));

		if (query != null && !"".equals(query))
			ccb.setQuery(ByteString.copyFromUtf8(query));
		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.CountDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void update(String collectionName, String query, String doc, CallBack callback){
		this.update(collectionName, query, doc, callback, false, false);
	}

	public void update(String collectionName, String query, String doc, CallBack callback, boolean upset, boolean multi){
		Protocol.UpdateDocRequest.Builder ccb = Protocol.UpdateDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));
		ccb.setQuery(ByteString.copyFromUtf8(query));
		ccb.setDoc(ByteString.copyFromUtf8(doc));

		ccb.setUpset(upset);
		ccb.setMulti(multi);

		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.UpdateDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void delete(String collectionName, String query, String doc, CallBack callback, boolean upset, boolean multi){
		Protocol.DeleteDocRequest.Builder ccb = Protocol.DeleteDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));
		ccb.setQuery(ByteString.copyFromUtf8(query));

		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.DeleteDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void insert(String collectionName, String doc, CallBack callback){
		Protocol.InsertDocRequest.Builder ccb = Protocol.InsertDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));
		ccb.setDoc(ByteString.copyFromUtf8(doc));

		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.InsertDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void createCollection(String collectionName, String doc, CallBack callback){
		Protocol.CreateCollectionRequest.Builder ccb = Protocol.CreateCollectionRequest.newBuilder();
		ccb.setCallbackId(ByteString.copyFromUtf8(ObjectId.get().toString()));
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));

		if (callback != null)
			ccb.setCallbackId(callback.getId());

		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.CreateCollectionRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}

	public void findAndModify(String collectionName, String query, CallBack callback, String update, String replace){
		this.findAndModify(collectionName, query, callback, update, replace, false, false, false);
	}

	public void findAndModify(String collectionName, String query, CallBack callback, String update, String replace, boolean remove, boolean upset, boolean rettype){
		Protocol.FindAndModifyDocRequest.Builder ccb = Protocol.FindAndModifyDocRequest.newBuilder();
		ccb.setDb(ByteString.copyFromUtf8("fish"));
		ccb.setCollection(ByteString.copyFromUtf8(collectionName));
		ccb.setQuery(ByteString.copyFromUtf8(query));

		if (callback != null)
			ccb.setCallbackId(callback.getId());
		if (update != null && !"".equals(update))
			ccb.setUpdate(ByteString.copyFromUtf8(update));
		if (replace != null && !"".equals(replace))
			ccb.setReplace(ByteString.copyFromUtf8(replace));

		ccb.setRemove(remove);
		ccb.setUpset(upset);
		ccb.setRettype(rettype);
		Protocol.DBMessage.Builder dbb = Protocol.DBMessage.newBuilder();
		dbb.setOp(Protocol.DBMessage.OpType.FindAndModifyDocRequest);
		dbb.setParameters(ccb.build().toByteString());

		Protocol.Request.Builder rb = Protocol.Request.newBuilder();
		rb.setCmdId(Protocol.Request.CmdIdType.DBMessage);
		rb.setExtension(Protocol.DBMessage.request, dbb.build());
		this.cf.channel().writeAndFlush(rb.build());
	}
}