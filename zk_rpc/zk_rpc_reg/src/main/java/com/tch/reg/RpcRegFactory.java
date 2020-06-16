package com.tch.reg;

import com.lagou.service.RpcRegistryHandler;
import com.lagou.service.ZkConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Service;

/**
 * @ClassName:RpcRegFactory
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/6/13
 */
@Service
public class RpcRegFactory implements FactoryBean<RpcRegistryHandler> {

    private ZkRegistryHandler zkRegistryHandler;
    private ZkConfig zkConfig;

    @Override
    public ZkRegistryHandler getObject() throws Exception {
        if(zkRegistryHandler != null){
            return zkRegistryHandler;
        }
        if(zkConfig == null){
            zkConfig = ZkConfig.getInstance();
        }
        zkRegistryHandler = new ZkRegistryHandler(zkConfig.getZkAddr());
        return zkRegistryHandler;
    }

    @Override
    public Class<?> getObjectType() {
        return RpcRegistryHandler.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}