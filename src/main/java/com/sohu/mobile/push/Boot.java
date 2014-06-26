package com.sohu.mobile.push;

import com.sohu.mobile.push.component.*;
import com.sohu.mobile.push.netty.CommandHandler;
import com.sohu.mobile.push.netty.ProtocolProcessInBoundHandler;
import com.sohu.mobile.push.netty.ProtocolProcessOutBoundHandler;
import com.sohu.mobile.push.netty.master.MasterHandler;
import com.sohu.mobile.push.netty.slave.SlaveMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import java.util.List;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jianjundeng on 3/6/14.
 */
public class Boot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Boot.class);

    private static ConfigComponent configComponent = ConfigComponent.getInstance();

    private static ZKComponent zkComponent = ZKComponent.getInstance();

    public static void main(String[] args) {
        String mode = System.getProperty("mode");
        if (mode != null && mode.equals("master")) {
            startMaster();
        } else if (mode != null && mode.equals("slave")) {
            startSlave();
            startListen();
        } else {
            startOther();
        }
    }

    private static void startOther() {
        new CleanThread().start();
    }

    public static void startMaster() {
        new FetchThread().start();
        zkComponent.publishMasterConfig(configComponent.getNotifyPort());
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolProcessInBoundHandler());
                            ch.pipeline().addLast(new ProtocolProcessOutBoundHandler());
                            ch.pipeline().addLast(new MasterHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            b.bind(configComponent.getNotifyPort()).sync();
        } catch (InterruptedException e) {
            LOGGER.error("interrupt", e);
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        LOGGER.info("master notify register to zookeeper");
        LOGGER.info("master notify server start");
    }

    public static void startSlave() {
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
                    ch.pipeline().addLast(new SlaveMessageHandler());
                }
            });

            List<String> masters = zkComponent.getMasterConfig();
            if (masters == null || masters.size() == 0) {
                LOGGER.error("master config not found,slave can not start");
                System.exit(0);
            }
            for (String connStr : masters) {
                String[] part = connStr.split(":");
                String host = part[0];
                int port = Integer.parseInt(part[1]);
                b.connect(host, port).sync();
                LOGGER.info("slave notify client start");
            }
        } catch (InterruptedException e) {
            LOGGER.error("interrupt", e);
            workerGroup.shutdownGracefully();
        }
    }

    public static void startListen() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolProcessInBoundHandler());
                            ch.pipeline().addLast(new ProtocolProcessOutBoundHandler());
                            ch.pipeline().addLast(new CommandHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 4096)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            b.bind(configComponent.getPushPort()).sync();
        } catch (InterruptedException e) {
            LOGGER.error("interrupt", e);
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        LOGGER.info("listen start");
    }
}
