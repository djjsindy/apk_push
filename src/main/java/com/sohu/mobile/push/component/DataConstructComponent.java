package com.sohu.mobile.push.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sohu.mobile.push.util.PushRSAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jianjundeng on 5/27/14.
 */
public class DataConstructComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataConstructComponent.class);

    private static final String privateKey = "MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEAm37gwrrEUqwnzXRU5IU4Roe3M6BXISSW4CCGYrZlVfbIoNdLzn12DytMlK9p12uYwMi8UvMrlhC2bfdSYFcDUwIDAQABAkBY-LksUudgkobXxTeJ5jw0BxWpYZZZLXFy3slmFA1ZqM33waLbC-lWi9zZVOMN_kPuctJtV9S39V1h89xvqzd5AiEAy2xpA7_tVcGn8_8BTv7OLuupa7H6y4ZUvNZaPAkfdE0CIQDDr02Y4BLg0RllwJowNUtENjHpghOz48hPCq9xJ5amHwIhAJYuc1lRWOb3EggRO-YWOCJr2aiovX-ErQq9NeEc2xwBAiEAkGlV3cU3iVwm3xXFdGBQPHW2XfpV6gLm95YOJXau0ccCIQDC48jEQcKwUsWlvPftdm24O-pLc97UidqAiUh8x9PvjA";

    private static ReentrantLock reentrantLock=new ReentrantLock();

    public static String construct(List<String> datas,boolean rsa){
        final JSONArray jsonArray = new JSONArray();
        for (String data : datas) {
            JSONObject jsonObject = JSON.parseObject(data);
            jsonArray.add(jsonObject);
        }
        String data=null;
        if (rsa) {
            try {
                reentrantLock.lock();
                data= PushRSAUtil.encodeSecretByPriKey(privateKey, jsonArray.toJSONString());
            } catch (Exception e) {
                LOGGER.error("write error,rsa", e);
            }finally {
                reentrantLock.unlock();
            }
        } else {
            data=jsonArray.toJSONString();
        }
        return data;
    }
}
