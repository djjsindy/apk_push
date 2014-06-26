package com.sohu.mobile.push;

import com.alibaba.fastjson.JSONObject;
import com.sohu.mobile.push.netty.ProtocolProcessInBoundHandler;
import com.sohu.mobile.push.netty.ProtocolProcessOutBoundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by jianjundeng on 3/18/14.
 */
public class TestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) throws InterruptedException {
        int start = Integer.parseInt(System.getProperty("start"));
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolProcessInBoundHandler());
                    ch.pipeline().addLast(new ProtocolProcessOutBoundHandler());
                    ch.pipeline().addLast(new HelloClientHandler());
                }
            });

            for (int i = start * 10000; i < (start + 1) * 10000; i++) {

                // 连接到本地的8000端口的服务端
                ChannelFuture channelFuture = b.connect(new InetSocketAddress(
                        "10.16.19.45", 10000)).sync();
                Channel channel = channelFuture.channel();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("devCode", "djjTest" + i);
                jsonObject.put("appId", "1001");
                jsonObject.put("command", 1);
                String data = jsonObject.toString();
                channel.write(data);
            }
        } catch (Exception e) {

        }
    }

    private static class HelloClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LOGGER.info("receive msg:" + msg);
        }

    }
}
