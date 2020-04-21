package com.dld.server;

public class CakTest {
    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        RpcServer rpcServer = new RpcServer();
        rpcServer.publish("com.dld.service");
        rpcServer.start();
    }
}
