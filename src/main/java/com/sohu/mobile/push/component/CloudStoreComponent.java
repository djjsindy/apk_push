package com.sohu.mobile.push.component;

import com.sohu.rediscloud.client.JedisX;
import com.sohu.rediscloud.client.factory.JedisXFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static com.sohu.mobile.push.util.Constant.*;

/**
 * Created by jianjundeng on 3/7/14.
 */
public class CloudStoreComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudStoreComponent.class);

    protected static JedisX pool;

    private CloudStoreComponent(){
        pool= new JedisXFactory("rediscloud.xml", "commonPool").getClient();
    }

    public static CloudStoreComponent getInstance() {
        return CloudStoreComponentHolder.INSTANCE;
    }

    private static class CloudStoreComponentHolder {
        private static final CloudStoreComponent INSTANCE = new CloudStoreComponent();
    }

    public void storeData(String id,String appId,String data,int expireTime){
        pool.setString(String.format(PUSH_DATA_KEY,id,appId),expireTime,data);
    }

    public String getFirstMsgId(String appId){
        return pool.lindexString(String.format(PUSH_DATA_TIME_LINE,appId),0);
    }

    public List<String> getTimeLine(String appId){
        return pool.lrangeString(String.format(PUSH_DATA_TIME_LINE,appId),0,-1);
    }

    public void storeTimeLine(String id,String appId){
        pool.rpushString(String.format(PUSH_DATA_TIME_LINE,appId),id);
    }

    public void storeDevTimeLine(String id,String appId,String devCode){
        pool.rpushString(String.format(DEV_PUSH_DATA_TIME_LINE,appId,devCode),id);
    }

    public String getFirstDevMsgId(String appId,String devCode){
        return pool.lindexString(String.format(DEV_PUSH_DATA_TIME_LINE,appId,devCode),0);
    }

    public List<String> getDevTimeLine(String appId,String devCode){
        return pool.lrangeString(String.format(DEV_PUSH_DATA_TIME_LINE,appId,devCode),0,-1);
    }

    public void storeDevCode(String appId,String devCode){
        pool.saddString(String.format(APP_DEV_SET_KEY,appId),devCode);
    }

    public Set<String> getDevCode(String appId){
        return pool.smemberString(String.format(APP_DEV_SET_KEY,appId));
    }

    public void remMsgId(String appId,String id){
        pool.lremString(String.format(PUSH_DATA_TIME_LINE,appId),1,id);
    }

    public void remDevMsgId(String appId,String devCode,String id){
        pool.lremString(String.format(DEV_PUSH_DATA_TIME_LINE,appId,devCode),1,id);
    }

    public String getData(String id,String appId){
        return pool.getString(String.format(PUSH_DATA_KEY,id,appId));
    }

    public void storeDevLastId(String appId,String devCode,String id,long expireTime){
        pool.setString(String.format(DEV_DATA_KEY,appId,devCode), (int) expireTime,id);
    }

    public String getDevLastId(String appId,String devCode){
        return pool.getString(String.format(DEV_DATA_KEY,appId,devCode));
    }

    public void delDevInfo(String appId,String devCode){
        pool.delete(String.format(DEV_DATA_KEY,appId,devCode));
    }

    public void storeAppId(String appId){
        pool.saddString(APPID_SET_KEY,appId);
    }

    public Set<String> getAppIds(){
        return pool.smemberString(APPID_SET_KEY);
    }

    public void delDevTimeLine(String appId,String devCode){
        pool.delete(String.format(DEV_PUSH_DATA_TIME_LINE,appId,devCode));
    }

    public void delDevSet(String appId,String devCode){
        pool.sremString(String.format(APP_DEV_SET_KEY,appId),devCode);
    }

    public Long getMsgTTL(String appId,String id){
        return pool.ttl(String.format(PUSH_DATA_KEY,id,appId));
    }
}
