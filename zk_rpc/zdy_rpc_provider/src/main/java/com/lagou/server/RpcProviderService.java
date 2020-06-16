package com.lagou.server;

import com.lagou.handler.UserServerHandler;
import com.lagou.service.*;
import com.tch.reg.RpcRegFactory;
import com.tch.reg.RpcServiceConfig;
import com.tch.reg.ZkRegistryHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName:RpcProviderService
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/6/13
 */
@Service
public class RpcProviderService implements InitializingBean, DisposableBean {
    private Logger logger = LoggerFactory.getLogger(RpcProviderService.class);
    private RpcRegFactory rpcRegFactory;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;
    private RpcServiceConfig config;

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> instanceCacheMap = new HashMap<>();
        instanceCacheMap.put(UserService.class.getName(),UserService.class);
        config = RpcServiceConfig.builder().setApplicationName("rpc-provider")
                .setPort(ZkConfig.getInstance().getPort())
                .setDelay(3000)
                .setServices(instanceCacheMap)
                .setProviderSide(true);
        startServer();
    }

    private void startServer() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast( new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new UserServerHandler());

                    }
                });
        String ip = "127.0.0.1";
        config.setIp(ip);
        int port = config.getPort();
        String appName = config.getApplicationName();
        ChannelFuture sync = serverBootstrap.bind(ip, port).sync();
        if(config.getDelay() > 0){
            Thread.sleep(config.getDelay());
        }
        logger.info("=======开始注册========");
        this.registry(ip,port,appName,config.getServices());
        logger.info("======启动成功，ip:"+ip+",port"+port);
        sync.channel().closeFuture().sync();
    }

    public void registry(String ip, int port ,String appName,Map<String,Object> serviceMap) throws Exception {
        if(MapUtils.isEmpty(serviceMap)){
            logger.error("没有找到要注册的服务");
            throw new RuntimeException("没有服务需要注册");
        }
        if(rpcRegFactory == null){
            rpcRegFactory = new RpcRegFactory();
        }
        ZkRegistryHandler zkRegistryHandler = rpcRegFactory.getObject();
        if(zkRegistryHandler == null){
            logger.error("zkRegistryHandler is null");
            throw new RuntimeException("zkRegistryHandler is null");
        }
        serviceMap.entrySet().stream().forEach(stringObjectEntry -> zkRegistryHandler.registry(stringObjectEntry.getKey(),ip,String.valueOf(port)));
    }
}