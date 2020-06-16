package com.tch.reg;

import com.lagou.service.RpcRegistryHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.NodeChangeListener;

import static org.apache.zookeeper.ZooDefs.OpCode.create;
import static org.apache.zookeeper.ZooDefs.OpCode.setACL;

/**
 * @ClassName:ZkRegiryHandler
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/6/13
 */
public class ZkRegistryHandler implements RpcRegistryHandler {
    private Logger logger = LoggerFactory.getLogger(ZkRegistryHandler.class);
    private static final  String Zk_RPC_ROOT = "/zk_rpc/";
    private static final String ZK_PATH_SPL= "/";
    private static final List<NodeChangeListener> listeners = new ArrayList<>();
    private final String url;
    private String charset = "UTF-8";
    private CuratorFramework client;
    private volatile boolean closed;
    private List<String> serviceList = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService REPORT_WORKER = Executors.newScheduledThreadPool(5);


    public ZkRegistryHandler(String url) {
        this.url = url;
        int timeout = 5000;
         client = CuratorFrameworkFactory.builder()
                .connectString(url).sessionTimeoutMs(timeout).connectionTimeoutMs(timeout)
                .retryPolicy(new RetryNTimes(1,1000)).build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
                logger.info("zk 连接状态发生改变" + newState);
                if(ConnectionState.CONNECTED.equals(newState)){
                    logger.info("注册中心连接成功");
                }else if(ConnectionState.LOST.equals(newState)){
                    logger.info("注册中心连接丢失");
                }
            }
        });
        client.start();
         //定时上报

    }

    @Override
    public boolean registry(String service, String ip, String port) {
        String zkPath = providerPath(service);
        if(! exists(zkPath)){
            create(zkPath,false);
        }
        String tempPath = zkPath + ZK_PATH_SPL + ip + ":" +port;
        create(tempPath, true);
        return true;
    }

    private void create(String zkPath,boolean isTemp) {
        try {
            if (!isTemp) {
//                client.create().forPath(zkPath);
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath);
                logger.info("创建永久节点："+zkPath);
                System.out.println("创建永久节点："+zkPath);
            }else{
//                client.create().withMode(CreateMode.EPHEMERAL).forPath(zkPath);
                client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zkPath);
                logger.info("创建临时节点：" + zkPath);
                System.out.println("创建临时节点：" + zkPath);
            }
        }catch (Exception e){
           e.printStackTrace();
            logger.error("创建节点失败"+e.getMessage());
        }
    }

    private String providerPath(String service) {
        return  Zk_RPC_ROOT + service + ZK_PATH_SPL + "provider";
    }

    @Override
    public List<String> discovery(String service) {
        String path = providerPath(service);
        try {
            if (serviceList.isEmpty()) {
                logger.info("首次从注册中心查找服务地址。。。。。。");
               serviceList = client.getChildren().forPath(path);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("服务发现失败", e.getMessage());
        }
        this.registryWatch(service,path);
        return serviceList;
    }

    private void registryWatch(String service, String path) {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

    }

    @Override
    public void addListener(NodeChangeListener listener) {

    }

    @Override
    public void destroy() {

    }

    public String getUrl() {
        return url;
    }

    private boolean exists(String path){
        try {
            if(client.checkExists().forPath(path) != null){
                return true;
            }
        } catch (Exception e) {
            logger.error("校验节点失败:"+e.getMessage());
            throw new IllegalStateException(e.getMessage(),e);
        }
        return false;
    }

    private String metricsPath(){
        return Zk_RPC_ROOT + "metrics";
    }
}