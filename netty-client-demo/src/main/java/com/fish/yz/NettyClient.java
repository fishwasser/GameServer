package com.fish.yz;

import com.fish.yz.service.ClientHandler;
import com.fish.yz.util.Config;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import com.google.protobuf.ExtensionRegistry;

import com.fish.yz.protobuf.Protocol;


public class NettyClient implements Runnable {
    private static ExtensionRegistry registry = ExtensionRegistry.newInstance();

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            Protocol.registerAllExtensions(registry);

            b.group(group);
            b.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("protobufDecoder", new ProtobufDecoder(Protocol.Request.getDefaultInstance(), registry));
                    pipeline.addLast("protobufEncoder", new ProtobufEncoder());

                    pipeline.addLast(new ClientHandler());
                }
            });
            for (int i = 0; i < 1; i++) {
                ChannelFuture f = b.connect(Config.instance().get("gate1.ip"), Integer.parseInt(Config.instance().get("gate1.port"))).sync();

                Protocol.Request.Builder requestBuilder = Protocol.Request.newBuilder();
                requestBuilder.setCmdId(Protocol.Request.CmdIdType.ConnectServerRequest);
                Protocol.ConnectServerRequest.Builder csBuilder = Protocol.ConnectServerRequest.newBuilder();
                csBuilder.setType(Protocol.ConnectServerRequest.RequestType.NEW_CONNECTION);
                csBuilder.setDeviceid(ByteString.copyFromUtf8("123456"));

                requestBuilder.setExtension(Protocol.ConnectServerRequest.request, csBuilder.build());
                Protocol.Request request = requestBuilder.build();

                f.channel().writeAndFlush(request);
                f.channel().closeFuture().sync();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
	    for (int i = 0; i < 1; i++) {
		    new Thread(new NettyClient(), ">>>this thread " + i).start();
	    }
    }
}