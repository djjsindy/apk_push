package com.sohu.mobile.push.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jianjundeng on 6/11/14.
 */
public class ProtocolProcessOutBoundHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolProcessOutBoundHandler.class);

    private byte specialR='\r';

    private byte specialN='\n';

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf channelBuffer= PooledByteBufAllocator.DEFAULT.directBuffer();
        String writeData=(String)msg;
        channelBuffer.writeInt(writeData.getBytes(DEFAULT_CHARSET).length);
        channelBuffer.writeByte(specialR);
        channelBuffer.writeByte(specialN);
        channelBuffer.writeBytes(writeData.getBytes(DEFAULT_CHARSET));
        channelBuffer.writeByte(specialR);
        channelBuffer.writeByte(specialN);
        ctx.writeAndFlush(channelBuffer);
        LOGGER.info("real write data,data:"+writeData);
        channelBuffer.release();
    }
}
