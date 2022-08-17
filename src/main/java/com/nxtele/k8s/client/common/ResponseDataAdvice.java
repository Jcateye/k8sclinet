package com.nxtele.k8s.client.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author zhangjincheng
 */
@RestControllerAdvice(basePackages = {"com.nxtele.app.controller","com.nxtele.app.whatsapp.bma.controller","com.nxtele.app.whatsapp.cloud.controller","com.nxtele.app.whatsapp.onpremises.controller"})
public class ResponseDataAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否对响应进行处理，为true表示需要进行包装，走beforeBodyWrite
     *
     * @param methodParameter
     * @param aClass
     * @return
     */
    @Override
    @SuppressWarnings("all") // 屏蔽所有的警告信息
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {

        // 如果Controller类被 IgnoreResponseAdvice注解，则不会进行处理
        if (methodParameter.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }
        // 如果方法被 IgnoreResponseAdvice注解，则不会进行处理
        if (methodParameter.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 定义最终的返回对象
        CommonResponse<Object> response = new CommonResponse<>(0, "");
        if (null == o) {
            // 是null，返回response
            return response;
        } else if (o instanceof CommonResponse) {
            // 本身就是CommonResponse，则强转
            response = (CommonResponse<Object>) o;
        } else {
            // 否则,讲结果设置到response中
            response.setData(o);
        }
        return response;
    }
}
