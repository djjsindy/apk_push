package com.sohu.mobile.push.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

/**
 * Created by jianjundeng on 6/11/14.
 */
public class ProtocolProcessInBoundHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf lengthBuffer;

    private ByteBuf commandBuffer;

    private int lastBytes=0;

    private byte specialR='\r';

    private byte specialN='\n';

    private static final String DEFAULT_CHARSET = "UTF-8";

    private ProtocolParseEnum status=ProtocolParseEnum.readLength;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        lengthBuffer = ctx.alloc().buffer();
        commandBuffer=ctx.alloc().buffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        lengthBuffer.release();
        commandBuffer.release();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf channelBuffer= (ByteBuf) msg;
        while(channelBuffer.readableBytes()!=0){
            byte b=channelBuffer.readByte();
            if(status==ProtocolParseEnum.readLength&&b==specialR){
                status=ProtocolParseEnum.lengthR;
            }else if(status==ProtocolParseEnum.readLength){
                lengthBuffer.writeByte(b);
            }else if(status==ProtocolParseEnum.lengthR&&b==specialN){
                status=ProtocolParseEnum.readCommand;
                lastBytes=lengthBuffer.readInt();
            }else if(status==ProtocolParseEnum.readCommand&&lastBytes>0){
                commandBuffer.writeByte(b);
                lastBytes--;
            }else if(status==ProtocolParseEnum.readCommand&&lastBytes==0&&b==specialR){
                status=ProtocolParseEnum.commandEnd;
            }else if(status==ProtocolParseEnum.commandEnd&&b==specialN){
                byte[] data=new byte[commandBuffer.readableBytes()];
                commandBuffer.readBytes(data);
                String command=new String(data,DEFAULT_CHARSET);
                ctx.fireChannelRead(command);
                lengthBuffer.clear();
                commandBuffer.clear();
                status=ProtocolParseEnum.readLength;
            }else{
                throw new Exception();
            }
        }
    }
}
