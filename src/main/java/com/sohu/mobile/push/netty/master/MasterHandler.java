package com.sohu.mobile.push.netty.master;

import com.sohu.mobile.push.register.ClientBootstrapHolder;
import com.sohu.mobile.push.register.ConnectionRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * Created by jianjundeng on 3/11/14.
 */
public class MasterHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error("connection error,address" + ctx.channel().remoteAddress()+":"+cause.getMessage(),cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectionRegistry.unregisterSlaveChannel(ctx.channel());
        LOGGER.info("slave disconnected,slave address:"+ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("slave connected,slave address:"+ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg.equals("master")){
            ConnectionRegistry.registerSlaveChannel(ctx.channel());
            LOGGER.info("slave register,slave address:"+ctx.channel().remoteAddress());
        }
    }
}
