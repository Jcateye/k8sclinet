package com.nxtele.k8s.client.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 反射常用工具类
 *
 * @author: xuxunjian
 * @date: 2022/7/6 11:52
 */
public class ReflectUtils {

    /**
     * 字段注解执行行为(避免循环引用)
     *
     * @param annotationClass 标记注解
     * @param object          实体对象
     * @param actionFunc      业务动作函数
     * @throws IllegalAccessException,StackOverflowError (可能循环引用的pojo类不能使用lombok@Data注解,
     *                                                   因为lombok重写了 hashcode(本类hashcode()是所有属性hashCode()相加...))
     */
    public static void annotationFieldInvoke(Class<? extends Annotation> annotationClass, Object object, Function<String, String> actionFunc) throws IllegalAccessException {
        Set<Object> existObjectSet = new HashSet<>();
        existObjectSet.add(object);
        innerAnnotationFieldInvoke(existObjectSet, annotationClass, object, actionFunc);
    }

    /**
     * 字段注解执行行为
     *
     * @param object     实体对象
     * @param actionFunc 业务动作函数
     */
    private static void innerAnnotationFieldInvoke(Set<Object> existObjectSet, Class<? extends Annotation> annotationClass, Object object, Function<String, String> actionFunc) throws IllegalAccessException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(annotationClass)) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(object);
            if (Objects.nonNull(value)) {
                // String类型直接解析(递归调用终点之一)
                if ((value instanceof String)) {
                    field.set(object, actionFunc.apply((String) value));
                } else if (value instanceof Map) {
                    for (Object mapValue : ((Map<?, ?>) value).values()) {
                        innerAnnotationFieldInvoke(existObjectSet, annotationClass, mapValue, actionFunc);
                    }
                    // 集合类型递归调用,解析内层对象是否需要执行 行为
                } else if (value instanceof Collection) {
                    for (Object listItem : (Collection<?>) value) {
                        innerAnnotationFieldInvoke(existObjectSet, annotationClass, listItem, actionFunc);
                    }
                } else {
                    // 已经加载过的类不再加载解析 避免死循环
                    if (existObjectSet.contains(value)) {
                        return;
                    } else {
                        // 实体类加入已加载缓存set
                        existObjectSet.add(value);
                        innerAnnotationFieldInvoke(existObjectSet, annotationClass, value, actionFunc);
                    }
                }
            }
        }
    }

    /**
     * 类上存在指定注解的字段集合
     *
     * @param annotationClass
     * @param clazz
     * @return
     */
    public static List<Field> hasAnnotation(Class<? extends Annotation> annotationClass, Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return Stream.of(fields)
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    /**
     * 类的get方法Map
     *
     * @param clazz
     * @return
     */
    public static Map<String, Method> getMethodMap(Class clazz) {
        Method[] methods = clazz.getMethods();
        return Stream.of(methods).
                collect(Collectors.toMap(method -> method.getName().replaceFirst("get", "").toLowerCase(Locale.ROOT),
                        method -> method, (key1, key2) -> key1));
    }

    public static Map<String, String> getFieldValueMap(Object obj) {
        Map<String, Method> methodMap = getMethodMap(obj.getClass());
        Map<String, String> valueMap = new HashMap<>();

        methodMap.keySet().forEach(key -> {
            Method method = methodMap.get(key);
            try {
                Object value = method.invoke(obj);
                valueMap.put(key, Objects.isNull(value) ? "" : value.toString());
            } catch (Exception ignore) {
            }
        });
        return valueMap;
    }
}
