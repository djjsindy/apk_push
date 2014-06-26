package com.sohu.mobile.push.component;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * Created with IntelliJ IDEA.
 * User: jianjundeng
 * Date: 8/20/13
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class KestrelComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(KestrelComponent.class);

    private static MemcachedClient client;

    private static final String NEWSKEY="news_key";

    private ConfigComponent configComponent=ConfigComponent.getInstance();

    private KestrelComponent() {
        try {
            client=new MemcachedClient(AddrUtil.getAddresses(configComponent.getKesterlConfig()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KestrelComponent getInstance() {
        return KestrelComponentHolder.INSTANCE;
    }

    private static class KestrelComponentHolder {
        private static final KestrelComponent INSTANCE = new KestrelComponent();
    }

    public String fetchInfo(){
        return (String) client.get(NEWSKEY);
    }

}
