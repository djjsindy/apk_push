package com.sohu.mobile.push.register;

import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jianjundeng on 3/12/14.
 */
public class ClientBootstrapHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientBootstrapHolder.class);

    private static Bootstrap clientBootstrap;

    private static Timer timer=new Timer();

    public static void setBootstrap(Bootstrap cbs){
        clientBootstrap=cbs;
    }

    public static void reconnect(final SocketAddress remoteAddress){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clientBootstrap.connect(remoteAddress);
                LOGGER.info("slave reconnect,remoteAddress:"+remoteAddress);
            }
        },5000);

    }


}
