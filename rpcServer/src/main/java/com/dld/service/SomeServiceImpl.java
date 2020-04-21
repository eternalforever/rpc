package com.dld.service;

public class SomeServiceImpl implements SomeService {
    @Override
    public String hello(String name) {
        return name + "欢迎你";
    }
}
