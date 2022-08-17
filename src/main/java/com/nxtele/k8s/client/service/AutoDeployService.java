package com.nxtele.k8s.client.service;


import com.nxtele.k8s.client.bean.ServiceConfig;
import com.nxtele.k8s.client.common.BusinessException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;

import java.io.IOException;
import java.util.List;

public interface AutoDeployService {


    List<ApiResponse> applyConfig(ServiceConfig serviceConfig, Boolean isCreate) throws IOException, BusinessException, ApiException;

    /**
     * @param obj KubernetesObject
     * @return
     * @throws ApiException
     */
    ApiResponse createSource(Object obj, String nameSpace) throws ApiException;


    /**
     * @param obj KubernetesObject
     * @return
     * @throws ApiException
     */
    ApiResponse deleteSource(Object obj, String nameSpace) throws ApiException;
}
