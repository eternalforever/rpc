package com.dld.server;


import com.dld.dto.Invocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

//channelInboundHandlerAdapter:不会释放msg
public class RpcServerHandler extends SimpleChannelInboundHandler<Invocation> {
    //解析client发送来的msg，然后从reigerMao获取信息
    private Map<String,Object> registerMap;
    public RpcServerHandler(Map<String,Object> registerMap){
        this.registerMap = registerMap;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Invocation msg) throws Exception {
    Object result = "没有该提供者";
        if (registerMap.containsKey(msg.getClassName())){
            Object invoker = registerMap.get(msg.getClassName());
             result = invoker.getClass().getMethod(msg.getMethodName(), msg.getParamTypes())
                    .invoke(invoker, msg.getParamValues());
            channelHandlerContext.writeAndFlush(result);
            channelHandlerContext.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
