package com.sohu.mobile.push.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sohu.mobile.push.netty.DevApp;
import com.sohu.mobile.push.register.ConnectionRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by jianjundeng on 3/7/14.
 */
public class FetchThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchThread.class);

    private KestrelComponent kestrelComponent = KestrelComponent.getInstance();

    private CloudStoreComponent cloudStoreComponent = CloudStoreComponent.getInstance();

    public void run() {
        while (true) {
            try {
                String data = kestrelComponent.fetchInfo();
                if (data == null) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }
                DevApp devApp=insertIntoCloudStore(data);
                LOGGER.info("analyze msg success,msg:"+data);
                //如果消息不带appId，直接忽略
                if(devApp.getAppId()==null){
                    LOGGER.error("have not appId,continue");
                    continue;
                }
                cloudStoreComponent.storeAppId(devApp.getAppId());

                LOGGER.info("write to slave msg,msg:"+data);
                //write to slave,slave write to client
                data=DataConstructComponent.construct(Arrays.asList(data),false);
                for(Channel channel:ConnectionRegistry.getSlaveChannels()){
                    channel.write(data);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

    /**
     * {t:"0",m:{id:"xxxx",c:"xxx",t:""}}表示限时消息
     * {t:"1",m:{c:"xxx",t:""}}表示实时消息
     *
     * 限时消息需要入库记录,返回appId
     * @param data
     */
    private DevApp insertIntoCloudStore(String data) {
        JSONObject jsonObject = JSON.parseObject(data);
        String type= jsonObject.getString("type");
        String appId=jsonObject.getString("appId");
        String devCode=jsonObject.getString("devCode");
        Integer expireTime=jsonObject.getInteger("expireTime");
        if(appId!=null&&type.equals("0")){
            String id=jsonObject.getJSONObject("msg").getString("id");
            if(devCode!=null){
                cloudStoreComponent.storeDevTimeLine(id,appId,devCode);
                cloudStoreComponent.storeDevCode(appId,devCode);
            }else{
                cloudStoreComponent.storeTimeLine(id,appId);
            }
            cloudStoreComponent.storeData(id,appId,data,expireTime);
        }
        DevApp devApp=new DevApp();
        devApp.setAppId(appId);
        if(devCode!=null){
            devApp.setDevCode(devCode);
        }
        return devApp;
    }
}
