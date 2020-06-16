package com.lagou.service;

import java.util.List;
import java.util.prefs.NodeChangeListener;

/**
 * @ClassName:RpcRegitryHandler
 * @Description: TODO
 * @Auth: tch
 */
public interface RpcRegistryHandler {

    /**
     * @Author tch
     * @Description 服务注册
     * @Date 15:59
     * @Param [service, ip, port]
     * @return boolean
     **/
    public boolean registry(String service,String ip,String port);

    /**
     * @Author tch
     * @Description 服务发现
     * @Date 16:00
     * @Param [service]
     * @return java.util.List<java.lang.String>
     **/
    public List<String> discovery(String service);

    /**
     * @Author tch
     * @Description  添加监听者
     * @Date 16:24
     * @Param [listener]
     * @return void
     **/
    public void addListener(NodeChangeListener listener);

    /**
     * @Author tch
     * @Description  注册中心销毁者
     * @Date 16:25
     * @Param []
     * @return void
     **/
    public void destroy();
}
