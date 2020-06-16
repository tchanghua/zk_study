package com.lagou.client;

import com.lagou.service.RpcRegistryHandler;
import com.lagou.service.UserService;
import com.lagou.service.ZkConfig;
import com.tch.reg.ZkRegistryHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;


//@SpringBootApplication(scanBasePackages = "com.lagou.client")
public class ClientBootStrap {
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final int DEFAULT_REPORT_INTERVAL = 5;

    public static void main(String[] args) throws InterruptedException {

        /**/

        /*ZkConfig zkConfig = ZkConfig.getInstance();
        zkConfig.setZkAddr(ZK_ADDRESS);
        zkConfig.setConsumerSide(true);
        SpringApplication.run(ClientBootStrap.class, args);*/
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(UserService.class.getName(),UserService.class);
        ZkConfig.getInstance().setConsumerSide(true);
        ZkConfig.getInstance().setInterVal(5);
        ZkConfig.getInstance().setZkAddr(ZK_ADDRESS);
        RpcRegistryHandler rpcRegistryHandler = new ZkRegistryHandler(ZK_ADDRESS);
        RpcConsumer rpcConsumer = new RpcConsumer(rpcRegistryHandler,serviceMap);
        UserService proxy = (UserService) rpcConsumer.createProxy(UserService.class);

        while (true){
            Thread.sleep(2000);
            proxy.sayHello("are you ok?");
            System.out.println("已响应");
        }

    }
}
