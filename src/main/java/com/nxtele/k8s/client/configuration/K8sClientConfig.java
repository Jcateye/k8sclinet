package com.nxtele.k8s.client.configuration;

import com.nxtele.k8s.client.properties.AutoDeployProperties;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Slf4j
@Profile({"dev", "test", "pro"})
public class K8sClientConfig {

    @Autowired
    private AutoDeployProperties autoDeployProperties;


    @Bean
    public ApiClient apiClient() {

        ApiClient client = null;
        try {
            client = Config.fromConfig(autoDeployProperties.getKubeConfig());

        } catch (Exception e) {
            log.error("kube config don't exist: {}", autoDeployProperties.getKubeConfig());
            client = new ClientBuilder().setBasePath(autoDeployProperties.getBasePath()).setVerifyingSsl(false).build();
//                .setAuthentication(new AccessTokenAuthentication("Token")).build();
        }
//        ApiClient client = new ClientBuilder().setBasePath(autoDeployProperties.getBasePath()).setVerifyingSsl(false).build();
//                .setAuthentication(new AccessTokenAuthentication("Token")).build();
//        Configuration.setDefaultApiClient(client);
        // 存放K8S的config文件的全路径
//        String kubeConfigPath = "D:\\ideaWorkSpace\\whats-app-api\\config\\k8sconfig";
//        // 以config作为入参创建的client对象，可以访问到K8S的API Server
//        ApiClient client = ClientBuilder
//                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
//                .build();

        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

        log.info("---k8s connected----\n {}", autoDeployProperties.getKubeConfig());
        return client;
    }

    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    @Bean
    public AppsV1Api appsV1Api(ApiClient apiClient) {
        return new AppsV1Api(apiClient);
    }

    @Bean
    public NetworkingV1Api networkingV1Api(ApiClient apiClient) {
        return new NetworkingV1Api(apiClient);
    }

    @Bean
    public BatchV1Api batchV1Api(ApiClient apiClient) {
        return new BatchV1Api(apiClient);
    }

    @Bean
    public RbacAuthorizationV1Api rbacAuthorizationV1Api(ApiClient apiClient) {
        return new RbacAuthorizationV1Api(apiClient);
    }


    @Bean
    public AutoscalingV1Api autoscalingV1Api(ApiClient apiClient) {
        return new AutoscalingV1Api(apiClient);
    }
}
