package com.dld.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcServer {
    //存放远程调用接口注册表
    private Map<String,Object> registryMap = new HashMap<>();
    //存放指定暴力的业务接口实现类
    private List<String> classCache = new ArrayList();
    //发布服务
    public void publish(String basePackage) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        cacheClassCache(basePackage);
        doRegister();
    }
    //将指定包中的业务接口的实现类名写入到classCache中
    private void cacheClassCache(String basePackage) {
        URL resource = this.getClass().getClassLoader().getResource
                (basePackage.replaceAll("\\.","/"));
        if (resource == null){
            return;
        }
        File dir = new File(resource.getFile());

        for (File file : dir.listFiles()){
            if (file.isDirectory()){
                cacheClassCache(basePackage + "." + file.getName());

            }else if (file.getName().endsWith(".class")){
                String fileName = file.getName().replace(".class", "");
                classCache.add(basePackage + "." + fileName);
            }
        }
        System.out.println(classCache);
    }
    //将制定包中的与业务接口是心累写入到注册表中
    private void doRegister() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classCache.size() == 0){
            return;
        }
        for (String className : classCache){
            Class<?> clazz = Class.forName(className);
            registryMap.put(clazz.getInterfaces()[0].getName(),clazz.newInstance());

        }
    }

    //启动服务
    public void  start(){
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parentGroup,childGroup)
                    //指定当前server的链接请求处理线程被占用时
                    //临时存放已经完成了三次握手的请求的队列长度
                    .option(ChannelOption.SO_BACKLOG,1024)
                    //指定使用心跳机制保证tpc场链接的存活性
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    channelPipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE,
                            ClassResolvers.cacheDisabled(null)));
                    channelPipeline.addLast(new ObjectEncoder());
                    channelPipeline.addLast(new RpcServerHandler(registryMap));

                }

            });
            ChannelFuture future =bootstrap.bind(8888).sync();
            System.out.println("服务端已启动，服务端口为：8888");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }


}
