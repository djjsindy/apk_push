package com.sohu.mobile.push.netty;

/**
 * Created by jianjundeng on 3/12/14.
 */
public class DevApp {

    private String devCode;

    private String appId;

    public DevApp(String devCode, String appId){
        this.appId=appId;
        this.devCode=devCode;
    }

    public DevApp(){}

    public String getDevCode() {
        return devCode;
    }

    public void setDevCode(String devCode) {
        this.devCode = devCode;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
