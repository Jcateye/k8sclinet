package com.nxtele.k8s.client.bean;

import lombok.Data;

import java.util.Map;

/**
 * @author: xuxunjian
 * @date: 2022/8/12 11:20
 */
@Data
public class ServiceConfig {

    private String nameSpace;
    private String serviceName;
    private Map<String, String> valueMap;
}
