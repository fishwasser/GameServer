package com.fish.yz.servive;

import com.fish.yz.protobuf.Protocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by fishman on 5/12/2016.
 *
 */
public class DbServiceHandler extends SimpleChannelInboundHandler<Protocol.Request> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Protocol.Request request) throws Exception {
        System.out.println("received from gate");

        switch (request.getCmdId()){
            case ConnectServerRequest: // ConnectServerRequest
                connectServer(channelHandlerContext, request);
                break;
            case EntityMessage: // EntityMessage
                entityMessage(channelHandlerContext, request);
                break;
        }
    }

    public void connectServer(ChannelHandlerContext ctx, Protocol.Request request){
        System.out.println("client connect server \n" + request);

        Protocol.Request.Builder requestBuilder = Protocol.Request.newBuilder();
        requestBuilder.setCmdId(Protocol.Request.CmdIdType.ConnectServerReply);
        Protocol.ConnectServerReply.Builder csBuilder = Protocol.ConnectServerReply.newBuilder();
        csBuilder.setType(Protocol.ConnectServerReply.ReplyType.CONNECTED)
                .setRoutes(request.getExtension(Protocol.ConnectServerRequest.request).getRoutes());

        requestBuilder.setExtension(Protocol.ConnectServerReply.request, csBuilder.build());
        Protocol.Request reply = requestBuilder.build();
        System.out.println("reply");
        ctx.channel().write(reply);
    }

    public void entityMessage(ChannelHandlerContext ctx, Protocol.Request request){
        Protocol.EntityMessage em = request.getExtension(Protocol.EntityMessage.request);
        System.out.println("client connect server" + request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }
}