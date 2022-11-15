package com.ivchenko.ioc.injector.util;

import com.google.common.reflect.Invokable;
import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.PostConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static Set<Class<?>> getClassesAnnotatedWith(
            Class<? extends Annotation> annotation,
            Collection<Class<?>> classes
    ) {
        return classes.stream()
                .filter(c -> c.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }

    public static boolean hasAutowiredConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors())
                .anyMatch(ReflectionUtils::isAutowired);
    }

    public static Set<Constructor<?>> getAllConstructors(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors())
                .collect(Collectors.toSet());
    }

    public static Set<Constructor<?>> getAutowiredConstructors(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors())
                .filter(ReflectionUtils::isAutowired)
                .collect(Collectors.toSet());
    }

    public static boolean hasNoArgsConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
    }

    public static Set<Method> getPostConstructorMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(ReflectionUtils::isPostConstructor)
                .collect(Collectors.toSet());
    }

    private static boolean isAutowired(Constructor<?> constructor) {
        return hasAnnotation(Autowired.class, constructor);
    }

    private static boolean isPostConstructor(Method method) {
        return hasAnnotation(PostConstructor.class, method);
    }

    private static boolean hasAnnotation(
            Class<? extends Annotation> annotation,
            Constructor<?> constructor
    ) {
        return Invokable.from(constructor).isAnnotationPresent(annotation);
    }

    private static boolean hasAnnotation(
            Class<? extends Annotation> annotation,
            Method method
    ) {
        return Invokable.from(method).isAnnotationPresent(annotation);
    }
}
