package com.lagou.client;

import com.lagou.service.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName:RpcClient
 * @Description: TODO
 * @Auth: tch
 * @Date: 2020/6/15
 */
public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String serviceName;
    private int port;
    private NioEventLoopGroup group;
    private String ip;
    private Channel channel;
    private UserClientHandler userClientHandler;

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NioEventLoopGroup getGroup() {
        return group;
    }

    public void setGroup(NioEventLoopGroup group) {
        this.group = group;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public UserClientHandler getUserClientHandler() {
        return userClientHandler;
    }

    public void setUserClientHandler(UserClientHandler userClientHandler) {
        this.userClientHandler = userClientHandler;
    }

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void initClient(String serviceName) throws InterruptedException {
        if(userClientHandler == null){
            userClientHandler = new UserClientHandler();
        }
        this.group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,3,3));
                        pipeline.addLast(new RpcEncoder(RpcRequest.class,new JSONSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcResponse.class,new JSONSerializer()));
                        pipeline.addLast(userClientHandler);
                    }
                });
        System.out.println("ip:"+ip + "port:"+port);
        this.channel = bootstrap.connect(ip, port).sync().channel();
        if(!isValid()){
            channel.close();
            return;
        }
        System.out.println("======启动客户端："+serviceName+",ip:"+ip+",port:"+port);
    }

    public boolean isValid(){
        if(this.channel != null){
            return true;
        }
        return false;
    }
}