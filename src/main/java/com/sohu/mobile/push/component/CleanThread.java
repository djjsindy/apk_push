package com.sohu.mobile.push.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by jianjundeng on 3/7/14.
 */
public class CleanThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanThread.class);

    private CloudStoreComponent cloudStoreComponent=CloudStoreComponent.getInstance();

    public void run(){
        while(true){
            try{
                Set<String> appIds=cloudStoreComponent.getAppIds();
                for(String appId:appIds){
                    flushPublicTimeLine(appId);
                    Set<String> devs=cloudStoreComponent.getDevCode(appId);
                    for(String dev:devs){
                        flushPrivateTimeList(appId,dev);
                    }
                }
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){
               LOGGER.error(e.toString());
            }
        }

    }

    private void flushPrivateTimeList(String appId, String dev) {
        List<String> ids=cloudStoreComponent.getDevTimeLine(appId, dev);
        if(ids==null){
            return;
        }
        for(String id:ids){
            String data=cloudStoreComponent.getData(id,appId);
            if(data==null){
                cloudStoreComponent.remDevMsgId(appId, dev, id);
                LOGGER.info("flush private msg,appId:"+appId+",dev:"+dev+",msgId:"+id);
            }
        }

    }

    /**
     * 根据expireTime 清除timeline中过期id
     * @param appId
     */
    private void flushPublicTimeLine(String appId) {
        List<String> ids=cloudStoreComponent.getTimeLine(appId);
        if(ids.size()==0){
            return;
        }
        for(String id:ids){
            String data=cloudStoreComponent.getData(id,appId);
            if(data==null){
                cloudStoreComponent.remMsgId(appId, id);
                LOGGER.info("flush public msg,appId:"+appId+",msgId:"+id);
            }
        }

    }
}
