package com.lagou;

import com.lagou.service.ZkConfig;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@ComponentScan(value = "com.lagou")
@SpringBootApplication(scanBasePackages = "com.lagou")
class ServerBootstrap {
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final int DEFAULT_REPORT_INTERVAL = 5;

    public static void main(String[] args) throws InterruptedException {
        int port = 8990;
        if(args.length > 0 && NumberUtils.isDigits(args[0])){
            port = Integer.parseInt(args[0]);
        }
        ZkConfig zkConfig = ZkConfig.getInstance();
        zkConfig.setPort(port);
        zkConfig.setZkAddr(ZK_ADDRESS);
        zkConfig.setProviderSide(true);
        SpringApplication.run(ServerBootstrap.class, args);

//        UserServiceImpl.startServer("127.0.0.1",8990);


    }



}
