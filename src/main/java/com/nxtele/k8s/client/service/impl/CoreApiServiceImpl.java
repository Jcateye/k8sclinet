package com.nxtele.k8s.client.service.impl;

import com.nxtele.k8s.client.service.CoreApiService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.nxtele.k8s.client.service.impl.AutoDeployServiceImpl.getMetadataName;

/**
 * @author: xuxunjian
 * @date: 2022/8/16 12:01
 */
@Service
public class CoreApiServiceImpl implements CoreApiService {
    @Autowired
    private CoreV1Api apiInstance;

//    private ApiResponse<V1Namespace> createNamespace(V1Namespace namespace) throws ApiException {
//        ApiResponse<V1Namespace> apiResponse;
//        String metadataName = getMetadataName(namespace);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespaceWithHttpInfo(namespace, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespaceWithHttpInfo(metadataName, namespace, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1Service> createService(V1Service service, String nameSpace) throws ApiException {
//        ApiResponse<V1Service> apiResponse;
//        String metadataName = getMetadataName(service);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedServiceWithHttpInfo(nameSpace, service, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedServiceWithHttpInfo(nameSpace, metadataName, service, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1Pod> createPod(V1Pod pod, String nameSpace) throws ApiException {
//        ApiResponse<V1Pod> apiResponse;
//        String metadataName = getMetadataName(pod);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedPodWithHttpInfo(nameSpace, pod, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedPodWithHttpInfo(nameSpace, metadataName, pod, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1Node> createNode(V1Node node) throws ApiException {
//        ApiResponse<V1Node> apiResponse;
//        String metadataName = getMetadataName(node);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNodeWithHttpInfo(node, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNodeWithHttpInfo(metadataName, node, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1Secret> createSecret(V1Secret secret, String nameSpace) throws ApiException {
//        ApiResponse<V1Secret> apiResponse;
//        String metadataName = getMetadataName(secret);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedSecretWithHttpInfo(nameSpace, secret, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedSecretWithHttpInfo(nameSpace, metadataName, secret, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1Endpoints> createEndpoints(V1Endpoints endpoints, String nameSpace) throws ApiException {
//        ApiResponse<V1Endpoints> apiResponse;
//        String metadataName = getMetadataName(endpoints);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedEndpointsWithHttpInfo(nameSpace, endpoints, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedEndpointsWithHttpInfo(nameSpace, metadataName, endpoints, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//
//    private ApiResponse<CoreV1Event> createEvent(CoreV1Event event, String nameSpace) throws ApiException {
//        ApiResponse<CoreV1Event> apiResponse;
//        String metadataName = getMetadataName(event);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedEventWithHttpInfo(nameSpace, event, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedEventWithHttpInfo(nameSpace, metadataName, event, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1PodTemplate> createPodTemplate(V1PodTemplate eodTemplate, String nameSpace) throws ApiException {
//        ApiResponse<V1PodTemplate> apiResponse;
//        String metadataName = getMetadataName(eodTemplate);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedPodTemplateWithHttpInfo(nameSpace, eodTemplate, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedPodTemplateWithHttpInfo(nameSpace, metadataName, eodTemplate, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1ConfigMap> createConfigMap(V1ConfigMap configMap, String nameSpace) throws ApiException {
//        ApiResponse<V1ConfigMap> apiResponse;
//        String metadataName = getMetadataName(configMap);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createNamespacedConfigMapWithHttpInfo(nameSpace, configMap, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replaceNamespacedConfigMapWithHttpInfo(nameSpace, metadataName, configMap, null, null, null, null);
//        }
//        return apiResponse;
//    }
//
//    private ApiResponse<V1PersistentVolume> createPersistentVolume(V1PersistentVolume persistentVolume, String nameSpace) throws ApiException {
//        ApiResponse<V1PersistentVolume> apiResponse;
//        String metadataName = getMetadataName(persistentVolume);
//        V1Namespace v1Namespace = apiInstance.readNamespace(metadataName, null);
//        if (Objects.isNull(v1Namespace)) {
//            apiResponse = apiInstance.createPersistentVolumeWithHttpInfo(persistentVolume, null, null, null, null);
//        } else {
//            apiResponse = apiInstance.replacePersistentVolumeWithHttpInfo(metadataName, persistentVolume, null, null, null, null);
//        }
//        return apiResponse;
//    }
}
