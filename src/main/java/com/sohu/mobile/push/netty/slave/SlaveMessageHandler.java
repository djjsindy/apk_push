package com.sohu.mobile.push.netty.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sohu.mobile.push.component.DataConstructComponent;
import com.sohu.mobile.push.register.ConnectionRegistry;
import com.sohu.mobile.push.util.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by jianjundeng on 3/11/14.
 */
public class SlaveMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlaveMessageHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message=(String)msg;
        LOGGER.info("receive msg from master,msg:"+msg);
        if(DateUtils.currentIsLimit()){
            LOGGER.info("receive msg from master,but msg is in 23-7");
            return;
        }
        JSONArray jsonArray= JSON.parseArray(message);
        for(Object o:jsonArray){
            JSONObject jsonObject=(JSONObject)o;
            String appId=jsonObject.getString("appId");
            String devCode=jsonObject.getString("devCode");
            final String data= DataConstructComponent.construct(Arrays.asList(jsonObject.toJSONString()),true);
            if(devCode!=null){
                final Channel channel=ConnectionRegistry.getChannel(devCode, appId);
                if(channel!=null){
                    LOGGER.info("push private msg,msg:"+jsonObject.toJSONString()+",devCode:"+devCode);
                    channel.writeAndFlush(data);
                }
            }else{
                for(Map.Entry<String,Channel> entry: ConnectionRegistry.allChannels(appId)){
                    LOGGER.info("push msg,msg:"+jsonObject.toJSONString()+",devCode:"+entry.getKey());
                    entry.getValue().writeAndFlush(data);
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.info("connect master exception,e:"+cause.toString());
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("master");
        LOGGER.info("connected master,master address:"+ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("disconnect master,master address:"+ctx.channel().remoteAddress());
    }

}
