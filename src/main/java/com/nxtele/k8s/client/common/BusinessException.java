package com.nxtele.k8s.client.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhangjincheng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends Exception{
    /** 错误码 */
    private Integer code;

    /** 错误消息 */
    private String message;
}
