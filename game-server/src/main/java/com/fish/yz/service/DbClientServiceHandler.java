package com.fish.yz.service;

import com.fish.yz.CallBack;
import com.fish.yz.DbClient;
import com.fish.yz.Repo;
import com.fish.yz.protobuf.Protocol;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by xscn2426 on 2016/12/3.
 *
 */
public class DbClientServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
		System.out.println("received from game manager, " + request);

		switch (request.getCmdId()){
			case DBMessage:
				System.out.println("received DBMessage, " + request);
				dealCallback(channelHandlerContext, request);
				break;
		}
	}

	private void dealCallback(ChannelHandlerContext ctx, Protocol.Request request) throws InvalidProtocolBufferException {
		Protocol.DBMessage msg = request.getExtension(Protocol.DBMessage.request);
		switch (msg.getOp()){
			case CreateCollectionReply:
				Protocol.CreateCollectionReply ccr = Protocol.CreateCollectionReply.parseFrom(msg.getParameters());
				callback(ccr.getCallbackId(), request);
				break;
			case CountDocReply:
				Protocol.CountDocReply cdr = Protocol.CountDocReply.parseFrom(msg.getParameters());
				callback(cdr.getCallbackId(), request);
				break;
			case FindDocReply:
				Protocol.FindDocReply fdr = Protocol.FindDocReply.parseFrom(msg.getParameters());
				callback(fdr.getCallbackId(), request);
				break;
			case UpdateDocReply:
				Protocol.UpdateDocReply udr = Protocol.UpdateDocReply.parseFrom(msg.getParameters());
				callback(udr.getCallbackId(), request);
				break;
			case InsertDocReply:
				Protocol.InsertDocReply idr = Protocol.InsertDocReply.parseFrom(msg.getParameters());
				callback(idr.getCallbackId(), request);
				break;
			case DeleteDocReply:
				Protocol.DeleteDocReply ddr = Protocol.DeleteDocReply.parseFrom(msg.getParameters());
				callback(ddr.getCallbackId(), request);
				break;
			case FindAndModifyDocReply:
				Protocol.FindAndModifyDocReply fmdr = Protocol.FindAndModifyDocReply.parseFrom(msg.getParameters());
				callback(fmdr.getCallbackId(), request);
				break;
		}
	}

	private void callback(ByteString id, Protocol.Request request){
		if (Repo.instance().callbacks.containsKey(id)){
			CallBack cb = Repo.instance().callbacks.get(id);
			cb.onResult(request);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exceptionCaught in db service " + cause.getMessage());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 失败重连
		final EventLoop loop = ctx.channel().eventLoop();
		loop.schedule(new Runnable() {
			public void run() {
				DbClient.instance().connectDbServer();
			}
		}, 3L, TimeUnit.SECONDS);
	}
}