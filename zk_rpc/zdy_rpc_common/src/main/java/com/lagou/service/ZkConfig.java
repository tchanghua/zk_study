package com.lagou.service;

/**
 * @ClassName:ZkConfig
 * @Description: 全局配置项
 * @Auth: tch
 * @Date: 2020/6/13
 */
public class ZkConfig {

    private static volatile ZkConfig zkConfig;
    private int port;
    private String zkAddr;
    //主动上报间隔时间
    private int interVal;

    private boolean consumerSide;

    private boolean providerSide;

    private ZkConfig(){}

    public static ZkConfig getInstance(){
        if(zkConfig == null){
            synchronized (ZkConfig.class){
                if(zkConfig == null){
                    zkConfig = new ZkConfig();
                }
            }
        }
        return zkConfig;
    }

    public static ZkConfig getZkConfig() {
        return zkConfig;
    }

    public static void setZkConfig(ZkConfig zkConfig) {
        ZkConfig.zkConfig = zkConfig;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZkAddr() {
        return zkAddr;
    }

    public void setZkAddr(String zkAddr) {
        this.zkAddr = zkAddr;
    }

    public int getInterVal() {
        return interVal;
    }

    public void setInterVal(int interVal) {
        this.interVal = interVal;
    }

    public boolean isConsumerSide() {
        return consumerSide;
    }

    public void setConsumerSide(boolean consumerSide) {
        this.consumerSide = consumerSide;
    }

    public boolean isProviderSide() {
        return providerSide;
    }

    public void setProviderSide(boolean providerSide) {
        this.providerSide = providerSide;
    }
}