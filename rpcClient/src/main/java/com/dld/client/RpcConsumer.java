package com.dld.client;

import com.dld.service.SomeService;

public class RpcConsumer {
    public static void main(String[] args) {
        SomeService someService = RpcProxy.create(SomeService.class);
        System.out.println(someService.hello("liqi"));

    }
}
