package com.dld.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class Invocation implements Serializable {
    /**
     * 远程服务名称
     */
    private String className;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] paramValues;
}
