package com.nxtele.k8s.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字段安全配置信息
 *
 * @author: xuxunjian
 * @date: 2022/7/12 17:05
 */
@ConfigurationProperties(prefix = "k8s.auto")
@Component
@Data
public class AutoDeployProperties {

    private String basePath;
    private String kubeConfig;
    private String servicePrefix;
    private String configPath;

    private Map<String, String> env;
}
