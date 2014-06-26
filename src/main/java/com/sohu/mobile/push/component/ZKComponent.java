package com.sohu.mobile.push.component;

import com.github.zkclient.ZkClient;
import com.sohu.mobile.push.util.IpUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jianjundeng on 3/11/14.
 */
public class ZKComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKComponent.class);

    private ZkClient zkClient;

    private ConfigComponent configComponent = ConfigComponent.getInstance();

    private static final String ZK_PATH = "/push_server_master_config";

    private ZKComponent() {
        zkClient = new ZkClient(configComponent.getZkConfig());
    }

    public static ZKComponent getInstance() {
        return ZKComponentHolder.INSTANCE;
    }

    private static class ZKComponentHolder {
        private static final ZKComponent INSTANCE = new ZKComponent();
    }

    public List<String> getMasterConfig() {
        try {
             return zkClient.getChildren(ZK_PATH);
        } catch (Exception e) {
            LOGGER.error("get master config error,e:" + e);
        }
        return null;
    }

    public void publishMasterConfig(int port) {
        String host = IpUtils.getLocalIp();
        boolean exist = zkClient.exists(ZK_PATH);
        if (!exist) {
            zkClient.create(ZK_PATH, null, CreateMode.PERSISTENT);
        }
        zkClient.create(ZK_PATH+"/"+host+":"+port,null,CreateMode.EPHEMERAL);
    }
}
