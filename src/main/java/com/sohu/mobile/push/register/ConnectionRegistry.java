package com.sohu.mobile.push.register;

import com.sohu.mobile.push.util.IpUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by jianjundeng on 3/7/14.
 */
public class ConnectionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionRegistry.class);

    private static ConcurrentHashMap<String,ConcurrentHashMap<String,Channel>> registry=new ConcurrentHashMap<String,ConcurrentHashMap<String,Channel>>();

    private static CopyOnWriteArraySet<Channel> slaveConnections=new CopyOnWriteArraySet<Channel>();

    public static void registerChannel(String devCode,String appId,Channel channel){
        ConcurrentHashMap<String,Channel> appMap=registry.get(appId);
        if(appMap==null){
            appMap=new ConcurrentHashMap<String, Channel>();
            registry.put(appId,appMap);
        }
        appMap.put(devCode, channel);
        String host= IpUtils.getLocalIp();
        LOGGER.info("host:"+host+",appId:"+appId+",connection:"+appMap.size());
    }

    public static void unregisterChannel(String devCode,String appId){
        ConcurrentHashMap<String,Channel> map=registry.get(appId);
        if(map!=null)
            map.remove(devCode);
    }

    public static Channel getChannel(String devCode,String appId){
        ConcurrentHashMap<String,Channel> map=registry.get(appId);
        if(map!=null)
            return map.get(devCode);
        return null;
    }

    public static Set<Map.Entry<String,Channel>> allChannels(String appId){
        ConcurrentHashMap<String,Channel>map=registry.get(appId);
        if(map!=null){
            return map.entrySet();
        }
        return Collections.emptySet();
    }

    public static void registerSlaveChannel(Channel channel){
        slaveConnections.add(channel);
        LOGGER.info("slaves size:"+slaveConnections.size());
    }

    public static void unregisterSlaveChannel(Channel channel){
        slaveConnections.remove(channel);
        LOGGER.info("slaves size:"+slaveConnections.size());
    }

    public static Set<Channel> getSlaveChannels(){
        LOGGER.info("write to slaves size:"+slaveConnections.size());
        return slaveConnections;
    }
}
