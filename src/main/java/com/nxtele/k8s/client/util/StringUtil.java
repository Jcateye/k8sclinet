package com.nxtele.k8s.client.util;


import com.nxtele.k8s.client.common.BusinessException;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: xuxunjian
 * @date: 2022/8/12 11:03
 */
public class StringUtil {

    public static String placeholderRegReplace(String reg, String source, Object obj) throws BusinessException {
        // 正则匹配出占位符 {{fieldName}},如"2893748923{{firstName}}testst{{lastName}}498" -> {{firstName}} {{lastName}}
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);
        Map<String, Method> nameMethodMap = ReflectUtils.getMethodMap(obj.getClass());
        // 替换值的函数
        Function<Object, String> func = param -> {
            try {
                if (param != null) {
                    Object value;
                    if (obj instanceof Map) {
                        value = ((Map<?, ?>) obj).get(param);
                    } else {
                        Method method = nameMethodMap.get(param);
                        value = method.invoke(obj);
                    }
                    return Objects.isNull(value) ? "" : GsonUtils.toString(value);
                }
            } catch (Exception ignored) {

            }
            return "";
        };

        StringBuffer result = new StringBuffer();
        for (; matcher.find(); ) {
            // 匹配到的占位符内的值
            String fieldName = matcher.group(2);
            // 替换占位符为实际值
            matcher.appendReplacement(result, Objects.requireNonNull(func.apply(fieldName)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String placeholderRegReplace(String reg, String source, Map<String, String> valueMap) throws BusinessException {
        // 正则匹配出占位符 {{fieldName}},如"2893748923{{firstName}}testst{{lastName}}498" -> {{firstName}} {{lastName}}
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(source);

        StringBuffer result = new StringBuffer();
        for (; matcher.find(); ) {
            // 匹配到的占位符内的值
            String fieldName = matcher.group(2).toLowerCase(Locale.ROOT);
            // 替换占位符为实际值
            matcher.appendReplacement(result, valueMap.get(fieldName));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
