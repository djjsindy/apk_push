package com.sohu.mobile.push.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jianjundeng
 * Date: 8/16/13
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigComponent.class);

    private static final Properties config=new Properties();


    private ConfigComponent(){
        try {
            config.load( ClassLoader.getSystemResourceAsStream("config.properties"));
        } catch (IOException e) {
            LOGGER.error("load config.properties  error",e);
        }
    }



    private static class ConfigComponentHolder {
        private static final ConfigComponent INSTANCE = new ConfigComponent();
    }

    public static ConfigComponent getInstance(){
        return ConfigComponentHolder.INSTANCE;
    }

    public String getKesterlConfig(){
        return config.getProperty("kesterl_server");
    }

    public String getZkConfig(){
        return config.getProperty("zkServer");
    }

    public int getPushPort(){
        String s=System.getProperty("pushPort");
        if(s==null){
            LOGGER.error("push port not set");
            System.exit(0);
        }
        return Integer.parseInt(s);
    }

    public int getNotifyPort(){
        String s=System.getProperty("notifyPort");
        if(s==null){
            LOGGER.error("notify port not set");
            System.exit(0);
        }
        return Integer.parseInt(s);
    }

}
