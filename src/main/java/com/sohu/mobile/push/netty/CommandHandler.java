package com.sohu.mobile.push.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sohu.mobile.push.component.CloudStoreComponent;
import com.sohu.mobile.push.component.DataConstructComponent;
import com.sohu.mobile.push.register.ConnectionRegistry;
import com.sohu.mobile.push.util.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by jianjundeng on 3/6/14.
 */
public class CommandHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private CloudStoreComponent cloudStoreComponent = CloudStoreComponent.getInstance();

    private static final String DEVAPP="devapp";

    private static final AttributeKey attributeKey=AttributeKey.valueOf(DEVAPP);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel channel = ctx.channel();
        String command = (String) msg;
        LOGGER.info("msg receive:"+command);
        JSONObject commandObject = JSON.parseObject(command);
        int operation = commandObject.getIntValue("command");
        String devCode;
        String appId;
        switch (operation) {
            case 2:
                devCode = ((DevApp) channel.attr(attributeKey).get()).getDevCode();
                appId = ((DevApp) channel.attr(attributeKey).get()).getAppId();
                String ackId = commandObject.getString("ackid");
                storeDevLastId(appId,devCode,ackId);
                LOGGER.info("devCode:" + devCode + ",ackId=" + ackId);
                break;
            case 1:
                devCode = commandObject.getString("devCode");
                appId = commandObject.getString("appId");
                if (devCode != null && appId != null) {
                    LOGGER.info("devCode:" + devCode + ",appId:" + appId + ",register");
                    DevApp channelContext = new DevApp(devCode, appId);
                    Attribute attribute=channel.attr(attributeKey);
                    attribute.set(channelContext);
                    if(!DateUtils.currentIsLimit()){
                        pushHistory(appId, devCode,channel);
                    }
                    ConnectionRegistry.registerChannel(devCode, appId, channel);
                }
                break;
            case 0:
                devCode = ((DevApp) channel.attr(attributeKey).get()).getDevCode();
                appId = ((DevApp) channel.attr(attributeKey).get()).getAppId();
                LOGGER.info("devCode:" + devCode + ",unregister");
                ConnectionRegistry.unregisterChannel(devCode, appId);
                cloudStoreComponent.delDevInfo(appId,devCode);
                cloudStoreComponent.delDevSet(appId,devCode);
                cloudStoreComponent.delDevTimeLine(appId,devCode);
                break;

        }

    }

    private void storeDevLastId(String appId, String devCode, String ackId) {
        long privateTTL=privateBeforeMaxTTL(appId,devCode,ackId);
        long publicTTL=publicBeforeMaxTTL(appId,ackId);
        long expireTime=Math.max(privateTTL,publicTTL);
        if(expireTime!=-1){
            cloudStoreComponent.storeDevLastId(appId, devCode, ackId,expireTime);
            LOGGER.info("store dev last Id,devCode:"+devCode+",appId:"+appId+",msgId:"+ackId+",expireTime:"+expireTime);
        }

    }

    private long privateBeforeMaxTTL(String appId,String devCode,String ack){
        List<String> devTimelines=cloudStoreComponent.getDevTimeLine(appId,devCode);
        long result=-1;
        int ackId=Integer.parseInt(ack);
        for(String s:devTimelines){
            int id=Integer.parseInt(s);
            if(id>ackId){
                break;
            }
            long ttl=cloudStoreComponent.getMsgTTL(appId,ack);
            if(ttl>result){
                result=ttl;
            }
        }
        return result;
    }

    private long publicBeforeMaxTTL(String appId,String ack){
        List<String> timelines=cloudStoreComponent.getTimeLine(appId);
        long result=-1;
        int ackId=Integer.parseInt(ack);
        for(String s:timelines){
            int id=Integer.parseInt(s);
            if(id>ackId){
                break;
            }
            long ttl=cloudStoreComponent.getMsgTTL(appId,ack);
            if(ttl>result){
                result=ttl;
            }
        }
        return result;
    }

    private void pushHistory(String appId,String devCode, Channel channel) {
        String lastPushId = cloudStoreComponent.getDevLastId(appId, devCode);
        if (lastPushId == null)
            lastPushId="0";
        ArrayList<String> ids=new ArrayList<String>();
        ids.addAll(findPrivateHistory(appId, devCode, lastPushId));
        ids.addAll(findPublicHistory(appId, lastPushId));
        Collections.sort(ids);
        List<String> msgs = new ArrayList<String>();
        for (String id : ids) {
            String msg = cloudStoreComponent.getData(id, appId);
            if (msg != null) {
                msgs.add(msg);
            }
        }
        if (msgs.size() > 0) {
            String data= DataConstructComponent.construct(msgs,true);
            LOGGER.info("push history msg:"+msgs+",devCode:"+devCode);
            channel.write(data);
        }
    }

    /**
     * ȥredis��ѯ������Ϣ
     * @param appId
     * @param lastPushId
     * @return
     */
    private List<String> findPublicHistory(String appId,String lastPushId) {
        String firstMsgId = cloudStoreComponent.getFirstMsgId(appId);
        if (firstMsgId == null)
            return Collections.emptyList();
        int storeFirstId = Integer.parseInt(firstMsgId);
        int lastId = Integer.parseInt(lastPushId);
        List<String> ids = cloudStoreComponent.getTimeLine(appId);
        if (lastId >=storeFirstId) {
            int index = ids.indexOf(lastPushId);
            if(index!=-1)
                ids = ids.subList(index + 1, ids.size());
        }
        return ids;
    }

    /**
     * ȥredis��ѯ����ĳ��dev��˽����Ϣ
     * @param devCode
     * @param appId
     * @param lastPushId
     * @return
     */
    private List<String> findPrivateHistory( String appId,String devCode,String lastPushId) {
        String firstMsgId = cloudStoreComponent.getFirstDevMsgId(appId,devCode);
        if (firstMsgId == null)
            return Collections.emptyList();
        int storeFirstId = Integer.parseInt(firstMsgId);
        int lastId = Integer.parseInt(lastPushId);
        List<String> ids = cloudStoreComponent.getDevTimeLine(appId,devCode);
        if (lastId >=storeFirstId) {
            int index = ids.indexOf(lastPushId);
            if(index!=-1)
                ids = ids.subList(index + 1, ids.size());
        }
        return ids;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel channel=ctx.channel();
        DevApp channelContext = ((DevApp) channel.attr(attributeKey).get());
        String devCode = channelContext==null?"":channelContext.getDevCode();
        LOGGER.error("connection error,address" + channel.remoteAddress()+":"+cause.toString()+",devCode:"+devCode);
        cause.printStackTrace();
        channel.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel=ctx.channel();
        DevApp channelContext = ((DevApp) channel.attr(attributeKey).get());
        String devCode = channelContext==null?"":channelContext.getDevCode();
        LOGGER.info("connection disconnect,address" + ctx.channel().remoteAddress()+",devCode:"+devCode);
        channelExceptionProcess(ctx);
    }

    private void channelExceptionProcess(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        DevApp channelContext = ((DevApp) channel.attr(attributeKey).get());
        String devCode = channelContext.getDevCode();
        String appId = channelContext.getAppId();
        Channel registryChannel = ConnectionRegistry.getChannel(devCode, appId);
        if (registryChannel != null && registryChannel.equals(channel))
            ConnectionRegistry.unregisterChannel(devCode, appId);
    }

}
