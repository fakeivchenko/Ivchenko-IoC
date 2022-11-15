package com.ivchenko.ioc.injector.util;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InjectionUtils {
    @SuppressWarnings("unchecked")
    public static <T> T getBeanInstance(
            Class<T> interfaceClass, Map<Class<?>, Class<?>> injectionMap, Map<Class<?>, Object> applicationScope
    ) {
        Class<?> implClass = getImplementationClass(interfaceClass, injectionMap);
        if (applicationScope.containsKey(implClass))
            return (T) applicationScope.get(implClass);
        else return null;
    }

    public static Class<?> getImplementationClass(Class<?> interfaceClass, Map<Class<?>, Class<?>> diMap) {
        Set<Map.Entry<Class<?>, Class<?>>> implClassesEntries = diMap.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(interfaceClass))
                .collect(Collectors.toSet());

        Preconditions.checkState(!implClassesEntries.isEmpty(), interfaceClass);

        if (implClassesEntries.size() > 1) {
            // TODO: 11/14/22 Qualifier impl
            return implClassesEntries.stream()
                    .findFirst()
                    .get()
                    .getKey();
        }

        // implClassesEntries size for sure equals 1
        return implClassesEntries.stream()
                .findFirst()
                .get()
                .getKey();
    }
}
