package com.lagou.client;

import com.lagou.service.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RpcConsumer {

    private RpcRegistryHandler handler;
    private Map<String,Object> serviceMap;
    private static List<RpcClient> rpcClients = new ArrayList<>();
    private Map<String,List<RpcClient>> clientMap = new HashMap<>();

    //创建线程池对象
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static UserClientHandler userClientHandler;

    //1.创建一个代理对象 providerName：UserService#sayHello are you ok?
    public Object createProxy(final Class<?> serviceClass){
        //借助JDK动态代理生成代理对象
        return  Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{serviceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //（1）调用初始化netty客户端的方法

                if(userClientHandler == null){
//                    userClientHandler = new UserClientHandler();
                   userClientHandler = rpcClients.get(0).getUserClientHandler();
//                    userClientHandler.channelActive();
//                    initClient();
                }

                //封装
                RpcRequest request = new RpcRequest();
                String requestId = UUID.randomUUID().toString();
                System.out.println(requestId);

                String className = method.getDeclaringClass().getName();
                String methodName = method.getName();

                Class<?>[] parameterTypes = method.getParameterTypes();

                request.setRequestId(requestId);
                request.setClassName(className);
                request.setMethodName(methodName);
                request.setParameterTypes(parameterTypes);
                request.setParameters(args);



                // 设置参数
                userClientHandler.setPara(request);
                System.out.println(request);
                System.out.println("设置参数完成");

                // 去服务端请求数据

                return executor.submit(userClientHandler).get();
            }
        });


    }



    //2.初始化netty客户端
    public static  void initClient() throws InterruptedException {
         userClientHandler = new UserClientHandler();

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(userClientHandler);
                    }
                });

        bootstrap.connect("127.0.0.1",8990).sync();

    }

    public RpcConsumer(RpcRegistryHandler handler, Map<String,Object> serviceMap){
        this.handler = handler;
        this.serviceMap = serviceMap;
        //开始自动注册消费者逻辑
        serviceMap.entrySet().forEach(new Consumer<Map.Entry<String, Object>>() {
            @Override
            public void accept(Map.Entry<String, Object> stringObjectEntry) {
                String serviceName = stringObjectEntry.getKey();
                List<String> discovery = handler.discovery(serviceName);
//                Cli
                for(String service : discovery){
                    String[] split = service.split(":");
                    RpcClient rpcClient = new RpcClient(split[0], Integer.parseInt(split[1]));
                    try{
                        rpcClient.initClient(serviceName);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    rpcClients.add(rpcClient);
                    clientMap.put(serviceName,rpcClients);
                }
            }
        });
//        handler.addListener(this);
    }


}
