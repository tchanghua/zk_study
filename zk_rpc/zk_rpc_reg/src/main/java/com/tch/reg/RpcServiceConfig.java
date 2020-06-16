package com.tch.reg;

import java.util.Map;

/**
 * @ClassName:RpcServiceConfig
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/6/13
 */
public class RpcServiceConfig {
    private String  applicationName;
    private int port;
    private int delay;
    private Map<String,Object> services;
    private boolean providerSide;
    private boolean consumerSide;
    private String ip;

    private RpcServiceConfig() {
    }

    public String getApplicationName() {
        return applicationName;
    }

    public RpcServiceConfig setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public int getPort() {
        return port;
    }

    public RpcServiceConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getDelay() {
        return delay;
    }

    public RpcServiceConfig setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public Map<String, Object> getServices() {
        return services;
    }

    public RpcServiceConfig setServices(Map<String, Object> services) {
        this.services = services;
        return this;
    }

    public boolean isProviderSide() {
        return providerSide;
    }

    public RpcServiceConfig setProviderSide(boolean providerSide) {
        this.providerSide = providerSide;
        return this;
    }

    public boolean isConsumerSide() {
        return consumerSide;
    }

    public RpcServiceConfig setConsumerSide(boolean consumerSide) {
        this.consumerSide = consumerSide;
        return this;
    }

    public static RpcServiceConfig builder(){
        return new RpcServiceConfig();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}