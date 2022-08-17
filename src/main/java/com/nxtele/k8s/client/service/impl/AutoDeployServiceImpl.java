package com.nxtele.k8s.client.service.impl;


import com.nxtele.k8s.client.bean.ServiceConfig;
import com.nxtele.k8s.client.common.BusinessException;
import com.nxtele.k8s.client.enumtype.Operation;
import com.nxtele.k8s.client.properties.AutoDeployProperties;
import com.nxtele.k8s.client.service.AutoDeployService;
import com.nxtele.k8s.client.util.GsonUtils;
import com.nxtele.k8s.client.util.ReflectUtils;
import com.nxtele.k8s.client.util.StringUtil;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AutoDeployServiceImpl implements AutoDeployService {
    @Autowired
    private CoreV1Api apiInstance;
    @Autowired
    private AppsV1Api appsV1Api;
    @Autowired
    private NetworkingV1Api networkingV1Api;
    @Autowired
    private BatchV1Api batchV1Api;
    @Autowired
    private RbacAuthorizationV1Api authorizationV1Api;
    @Autowired
    private AutoscalingV1Api autoscalingV1Api;
    @Autowired
    private AutoDeployProperties autoDeployProperties;

    public static final String SUFFIX_YAML = ".yaml";

    /**
     * 创建或者删除k8s资源和配置
     *
     * @param serviceConfig
     * @param isCreate
     * @return
     * @throws IOException
     * @throws BusinessException
     * @throws ApiException
     */
    public List<ApiResponse> applyConfig(ServiceConfig serviceConfig, Boolean isCreate) throws IOException, BusinessException, ApiException {
        List<ApiResponse> result = new ArrayList<>();
        String configPath = autoDeployProperties.getConfigPath();
        File file = new File(configPath);
        if (!file.exists()) {
            throw new FileNotFoundException(configPath);
        }
        // 遍历文件解析配置
        if (file.isDirectory()) {
            File[] files = file.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
            for (int i = 0; i < files.length; i++) {
                // 解析文件
                String configStr = parseFile(files[i], serviceConfig);
                List<ApiResponse> apiResponses;
                // 创建或者删除资源
                if (isCreate) {
                    apiResponses = create(serviceConfig.getNameSpace(), configStr);
                } else {
                    apiResponses = remove(serviceConfig.getNameSpace(), configStr);
                }
                result.addAll(apiResponses);

            }
        } else {
            String configStr = parseFile(file, serviceConfig);
            List<ApiResponse> apiResponses;
            if (isCreate) {
                apiResponses = create(serviceConfig.getNameSpace(), configStr);
            } else {
                apiResponses = remove(serviceConfig.getNameSpace(), configStr);
            }
            result.addAll(apiResponses);
        }

        return result;
    }

    private List<ApiResponse> create(String nameSpace, String configStr) throws IOException, ApiException {
        List<ApiResponse> result = new ArrayList<>();
        // 生成配置文件
        writeConfigFiles(nameSpace, configStr);
        // todo fix bug  io.kubernetes.client.util.ModelMapper.initModelMap:382?[0;39m | No kubernetes api model classes found from classloader, this may break automatic api discovery
        List<Object> objects = Yaml.loadAll(configStr);
        for (Object obj : objects) {
            // k8s生成资源
            result.add(createSource(obj, nameSpace));
        }
        return result;
    }

    private List<ApiResponse> remove(String nameSpace, String configStr) throws IOException, ApiException {
        List<ApiResponse> result = new ArrayList<>();
        // 删除配置文件
        removeConfigFile(nameSpace);
        List<Object> objects = Yaml.loadAll(configStr);
        for (Object obj : objects) {
            // k8s删除资源
            result.add(deleteSource(obj, nameSpace));
        }
        return result;
    }

    /**
     * 解析配置文件
     *
     * @param yamlFile
     * @param serviceConfig
     * @throws IOException
     * @throws BusinessException
     * @throws ApiException
     */
    private String parseFile(File yamlFile, ServiceConfig serviceConfig) throws IOException, BusinessException, ApiException {
        StringBuilder sb = new StringBuilder();
        // 读取文件
        try (BufferedReader reader = new BufferedReader(new FileReader(yamlFile))) {
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str).append(System.lineSeparator());
            }
        }
        // 替换占位符
        String yamlStrResult = StringUtil.placeholderRegReplace("(\\{\\{(.+?)\\}\\})", sb.toString(), getEnvValue(serviceConfig));
        log.info("config yaml -> \n{}", yamlStrResult);
        return yamlStrResult;
    }

    /**
     * 设置常量 优先级: http请求内常量 > http结构体配置 > 应用配置
     *
     * @param serviceConfig
     * @return
     */
    private Map<String, String> getEnvValue(ServiceConfig serviceConfig) {
        Map<String, String> envVariableMap = new HashMap<>();
        putAll(envVariableMap, autoDeployProperties.getEnv());
        putAll(envVariableMap, serviceConfig.getValueMap());
        putAll(envVariableMap, ReflectUtils.getFieldValueMap(serviceConfig));
        return envVariableMap;
    }

    /**
     * 生成配置文件
     *
     * @param nameSpace
     * @param yamlStrResult
     * @throws IOException
     */
    private void writeConfigFiles(String nameSpace, String yamlStrResult) throws IOException {
        // 生成配置文件名
        String serviceFileName = autoDeployProperties.getServicePrefix() + nameSpace + SUFFIX_YAML;
        String configPath = autoDeployProperties.getConfigPath();

        String subConfig = configPath + System.getProperty("file.separator") + "config-" + nameSpace;
        File subConfigDir = new File(subConfig);
        if (!subConfigDir.exists()) {
            log.info("create config directory:{}", subConfigDir);
            if (!subConfigDir.mkdir()) {
                log.error("dir [{}] create failed!", subConfigDir);
            }
        }

        File serviceConfigFile = new File(subConfigDir, System.getProperty("file.separator") + serviceFileName);
        log.info("create config file:{}", serviceConfigFile);
        if (!serviceConfigFile.createNewFile()) {
            log.error("file [{}] create failed!", serviceConfigFile);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(serviceConfigFile))) {
            bw.write(yamlStrResult);
            bw.flush();
        }
    }

    /**
     * 删除配置文件
     *
     * @param namespace
     * @return
     */
    private void removeConfigFile(String namespace) {
        String subConfig = autoDeployProperties.getConfigPath() + System.getProperty("file.separator") + "config-" + namespace;
        File subConfigDir = new File(subConfig);
        if (subConfigDir.exists()) {
            log.info("delete config directory:{}", subConfig);
            deleteDir(subConfigDir);
        }
    }

    private void deleteDir(File subConfigDir) {
        File[] files = subConfigDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }
        subConfigDir.delete();
    }

    public ApiResponse createSource(Object obj, String nameSpace) {
        ApiResponse apiResponse = null;
        String metadataName = getMetadataName(obj);
        try {
            log.info("---- create request resource---: \n {}", metadataName);
            if (obj instanceof V1Namespace) {
                apiResponse = this.apiInstance.createNamespaceWithHttpInfo((V1Namespace) obj,  (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1Service) {
                apiResponse = this.apiInstance.createNamespacedServiceWithHttpInfo(nameSpace, (V1Service) obj, (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1Pod) {
                apiResponse = this.apiInstance.createNamespacedPodWithHttpInfo(nameSpace, (V1Pod) obj, (String) null,  (String) null, (String) null,null);
            } else if (obj instanceof V1Node) {
                apiResponse = this.apiInstance.createNodeWithHttpInfo((V1Node) obj, (String) null,  (String) null, (String) null,null);
            } else if (obj instanceof V1Secret) {
                apiResponse = this.apiInstance.createNamespacedSecretWithHttpInfo(nameSpace, (V1Secret) obj,  (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1Endpoints) {
                apiResponse = this.apiInstance.createNamespacedEndpointsWithHttpInfo(nameSpace, (V1Endpoints) obj, (String) null, (String) null, (String) null,null);
            } else if (obj instanceof CoreV1Event) {
                apiResponse = this.apiInstance.createNamespacedEventWithHttpInfo(nameSpace, (CoreV1Event) obj,  (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1PodTemplate) {
                apiResponse = this.apiInstance.createNamespacedPodTemplateWithHttpInfo(nameSpace, (V1PodTemplate) obj,  (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1ConfigMap) {
                apiResponse = this.apiInstance.createNamespacedConfigMapWithHttpInfo(nameSpace, (V1ConfigMap) obj,  (String) null, (String) null, (String) null,null);
            } else if (obj instanceof V1PersistentVolume) {
                apiResponse = this.apiInstance.createPersistentVolumeWithHttpInfo((V1PersistentVolume) obj, (String) null,  (String) null, (String) null,null);
            } else if (obj instanceof V1PersistentVolumeClaim) {
                apiResponse = this.apiInstance.createNamespacedPersistentVolumeClaimWithHttpInfo(nameSpace, (V1PersistentVolumeClaim) obj, null, (String) null, (String) null, (String) null);
            } else if (obj instanceof V1ReplicaSet) {
                apiResponse = this.appsV1Api.createNamespacedReplicaSetWithHttpInfo(nameSpace, (V1ReplicaSet) obj, (String) null, (String) null,null, (String) null);
            } else if (obj instanceof V1Deployment) {
                apiResponse = this.appsV1Api.createNamespacedDeploymentWithHttpInfo(nameSpace, (V1Deployment) obj, (String) null,  (String) null, null,(String) null);
            } else if (obj instanceof V1Ingress) {
                apiResponse = this.networkingV1Api.createNamespacedIngressWithHttpInfo(nameSpace, (V1Ingress) obj, (String) null,  (String) null, null,(String) null);
            } else if (obj instanceof V1Job) {
                apiResponse = this.batchV1Api.createNamespacedJobWithHttpInfo(nameSpace, (V1Job) obj, (String) null, (String) null, null, (String) null);
            } else if (obj instanceof V1Role) {
                apiResponse = this.authorizationV1Api.createNamespacedRoleWithHttpInfo(nameSpace, (V1Role) obj, (String) null, (String) null, null,(String) null);
            } else if (obj instanceof V1HorizontalPodAutoscaler) {
                apiResponse = this.autoscalingV1Api.createNamespacedHorizontalPodAutoscalerWithHttpInfo(nameSpace, (V1HorizontalPodAutoscaler) obj,null, (String) null, (String) null, (String) null);
            } else {
                log.error(" not support type: {}", obj.getClass());
            }
        } catch (Exception e) {
            log.error("create invoke failed, resource: {}\n, response: {}\n, {} ", metadataName, apiResponse, e.getMessage());
        }
        log.info("----create response---: \n {}", GsonUtils.toJSONString(apiResponse));
        return apiResponse;
    }

    public ApiResponse deleteSource(Object obj, String nameSpace) {
        ApiResponse apiResponse = null;
        String metadataName = getMetadataName(obj);
        log.info("---- delete request resource---: \n {}", metadataName);
        try {
            if (obj instanceof V1Namespace) {
                apiResponse = this.apiInstance.deleteNamespaceWithHttpInfo(metadataName, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Service) {
                apiResponse = this.apiInstance.deleteNamespacedServiceWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Pod) {
                apiResponse = this.apiInstance.deleteNamespacedPodWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Node) {
                apiResponse = this.apiInstance.deleteNodeWithHttpInfo(metadataName, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Secret) {
                apiResponse = this.apiInstance.deleteNamespacedSecretWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Endpoints) {
                apiResponse = this.apiInstance.deleteNamespacedEndpointsWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof CoreV1Event) {
                apiResponse = this.apiInstance.deleteNamespacedEventWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1PodTemplate) {
                apiResponse = this.apiInstance.deleteNamespacedPodTemplateWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1ConfigMap) {
                apiResponse = this.apiInstance.deleteNamespacedConfigMapWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1PersistentVolume) {
                apiResponse = this.apiInstance.deletePersistentVolumeWithHttpInfo(metadataName, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1PersistentVolumeClaim) {
                apiResponse = this.apiInstance.deleteNamespacedPersistentVolumeClaimWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1ReplicaSet) {
                apiResponse = this.appsV1Api.deleteNamespacedReplicaSetWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Deployment) {
                apiResponse = this.appsV1Api.deleteNamespacedDeploymentWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Ingress) {
                apiResponse = this.networkingV1Api.deleteNamespacedIngressWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Job) {
                apiResponse = this.batchV1Api.deleteNamespacedJobWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1Role) {
                apiResponse = this.authorizationV1Api.deleteNamespacedRoleWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else if (obj instanceof V1HorizontalPodAutoscaler) {
                apiResponse = this.autoscalingV1Api.deleteNamespacedHorizontalPodAutoscalerWithHttpInfo(metadataName, nameSpace, (String) null, (String) null, (Integer) null, (Boolean) null, (String) null, new V1DeleteOptions());
            } else {
                log.error(" not support type: {}", obj.getClass());
            }
        } catch (Exception e) {
            log.error("delete invoke failed, resource: {} , response: {}\n, {}\n", metadataName, apiResponse, e.getMessage());
        }

        log.info("---- delete response---: \n {}", GsonUtils.toJSONString(apiResponse));
        return apiResponse;
    }

    public static String getMetadataName(Object obj) {
        if (obj instanceof KubernetesObject) {
            return ((KubernetesObject) obj).getMetadata().getName();
        } else {
            return "";
        }
    }

    private void putAll(Map<String, String> targetMap, Map<String, String> sourceMap) {
        if (MapUtils.isNotEmpty(sourceMap)) {
            targetMap.putAll(sourceMap);
        }
    }


    private ApiResponse invokeSourceOperation(Operation operation) {
        ApiResponse apiResponse = null;
        if (Operation.CREATE.equals(operation)) {

        } else if (Operation.UPDATE.equals(operation)) {

        } else if (Operation.DELETE.equals(operation)) {

        }
        return apiResponse;
    }

}
