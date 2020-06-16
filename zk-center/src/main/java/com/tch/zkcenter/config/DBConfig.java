package com.tch.zkcenter.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName:DBConfig
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/5/13
 */
public class DBConfig {

    private static Map<String,String> map = new HashMap<>();
    private static ThreadLocal<DruidDataSource> threadLocal = new ThreadLocal();
    private static CuratorFramework zkClient = null;
    private static final String CONFIG_PREFIX = "/CONFIG";

    public static Connection getConnection(){
        if(threadLocal.get() != null){
            try{
                DruidPooledConnection connection = threadLocal.get().getConnection();
                return threadLocal.get().getConnection();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void init() throws Exception {
        zkClient = CuratorFrameworkFactory.newClient("127.0.0.1:2181",new RetryNTimes(3,5000));
        zkClient.start();
        getConfig();
        initDataSource();
        startListener();
    }

    private static void startListener() {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,CONFIG_PREFIX,true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                String path = pathChildrenCacheEvent.getData().getPath();
                System.out.println("该节点数据产生变更："+new String(pathChildrenCacheEvent.getData().getData()));
                if(path.startsWith(CONFIG_PREFIX)){
                    String  key = path.replace(CONFIG_PREFIX +"/","");
                    if(PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(pathChildrenCacheEvent.getType())){
                        System.out.println("更新配置信息");
                        map.put(key,new String(pathChildrenCacheEvent.getData().getData()));
                        if(threadLocal.get()!= null){
                            threadLocal.get().close();
                        }
                        initDataSource();
                        System.out.println("更新连接池");
                    }
                }
                pathChildrenCache.start();
            }
        });
    }

    private static void initDataSource() throws SQLException{
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(map.get("driverClass"));
        dataSource.setUrl(map.get("jdbcUrl"));
        dataSource.setUsername(map.get("userName"));
        dataSource.setPassword(map.get("password"));
        threadLocal.set(dataSource);
    }

    private static void getConfig() throws Exception{
        List<String> childrenNames = zkClient.getChildren().forPath(CONFIG_PREFIX);
        for (String childName:childrenNames) {
            String value = new String(zkClient.getData().forPath(CONFIG_PREFIX+"/"+childName));
            map.put(childName,value);
        }
    }
}