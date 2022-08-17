package com.nxtele.k8s.client.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangjincheng
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 对系统中所有的异常进行拦截
     *
     * @param req
     * @param ex
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResponse<String> handlerCommerceException(HttpServletRequest req, Exception ex) {
        CommonResponse<String> response = new CommonResponse<>(ErrorCode.SYSTEM_ERROR, "SYSTEM ERROR");
        // validate校验 参数异常固定Code = 400
        if (ex instanceof BindException) {
            String defaultMessage = ((BindException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage();
            response.setCode(ErrorCode.PARAM_ERROR);
            response.setMessage(defaultMessage);
        } else if (ex instanceof MethodArgumentNotValidException) {
            // validate校验 参数异常固定code = 400
            String defaultMessage = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().get(0).getDefaultMessage();
            response.setCode(ErrorCode.PARAM_ERROR);
            response.setMessage(defaultMessage);
        } else if (ex instanceof BusinessException) {
            // 处理自定义异常, code 自定义
            BusinessException businessException = (BusinessException) ex;
            response.setCode(businessException.getCode());
            response.setMessage(businessException.getMessage());
        } else {
            // 系统异常 固定code = 500
            response.setMessage(ex.getMessage());
        }
        log.error("whats app service has error : [{}]", ex.getMessage(), ex);
        return response;
    }
}
