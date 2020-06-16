package com.tch.zkcenter;

import com.tch.zkcenter.config.DBConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;

@SpringBootApplication
public class ZkCenterApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ZkCenterApplication.class, args);
        DBConfig.init();
        Connection connection = DBConfig.getConnection();
        System.out.println("数据库连接信息：" + connection);
    }

}
