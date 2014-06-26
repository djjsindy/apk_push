package com.sohu.mobile.push.component;

import com.alibaba.fastjson.JSONObject;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.omg.CORBA.TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by jianjundeng on 3/12/14.
 */
public class TestThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestThread.class);

    public void run() {
        while(true){
        Jedis jedis = new Jedis("192.168.12.117", 6383);
        long id = jedis.incr("app_id_1001");
        MemcachedClient client = null;
        try {
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<5;i++){
                sb.append(UUID.randomUUID());
            }
            client = new MemcachedClient(AddrUtil.getAddresses("10.13.82.144:22133"));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 0);
            jsonObject.put("appId", 1001);
            jsonObject.put("expireTime",60*60);
            JSONObject msg = new JSONObject();
            jsonObject.put("msg", msg);
            msg.put("id", id);
            msg.put("title", "title" + id);
            msg.put("content", sb.toString());
            client.set("news_key", 0, jsonObject.toJSONString());
            LOGGER.info("send test data,msg"+jsonObject.toJSONString());
            TimeUnit.SECONDS.sleep(60*30);
        } catch (Exception e) {
            e.printStackTrace();
        }

        }
    }
}
