package com.nxtele.k8s.client.controller;

import com.nxtele.k8s.client.bean.ServiceConfig;
import com.nxtele.k8s.client.service.AutoDeployService;
import io.kubernetes.client.openapi.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "K8s客户端")
@RestController
@RequestMapping("deploy")
public class K8sClientController {

    @Autowired
    private AutoDeployService autoDeployService;


    @ApiOperation(value = "资源创建", notes = "", httpMethod = "POST")
    @PostMapping
    public List<ApiResponse> create(@RequestBody ServiceConfig serviceConfig) throws Exception {
        return autoDeployService.applyConfig(serviceConfig, true);
    }

    @ApiOperation(value = "资源删除", notes = "", httpMethod = "DELETE")
    @DeleteMapping
    public List<ApiResponse> delete(@RequestBody ServiceConfig serviceConfig) throws Exception {
        return autoDeployService.applyConfig(serviceConfig, false);
    }
}
