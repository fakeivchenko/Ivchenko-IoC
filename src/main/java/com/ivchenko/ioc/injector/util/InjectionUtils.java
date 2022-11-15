package com.ivchenko.ioc.injector.util;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class which contains methods used by Injector
 * @implNote Uses Google Guava Preconditions
 */
public class InjectionUtils {
    /**
     * ReturnsiInstance of the implementation class for specified interface class
     * @return instance of the implementation class for specified interface class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanInstance(
            Class<T> interfaceClass,
            Map<Class<?>, Class<?>> injectionMap,
            Map<Class<?>, Object> applicationScope,
            String qualifier
    ) {
        Class<?> implClass = getImplementationClass(interfaceClass, injectionMap, qualifier);
        if (applicationScope.containsKey(implClass))
            return (T) applicationScope.get(implClass);
        else return null;
    }

    /**
     * Returns implementation class for specified interface class
     * @return implementation class for specified interface class
     */
    public static Class<?> getImplementationClass(
            Class<?> interfaceClass,
            Map<Class<?>, Class<?>> diMap,
            String qualifier
    ) {
        // Finding all registered implementation classes for interfaceClass
        Set<Map.Entry<Class<?>, Class<?>>> implClassesEntries = diMap.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(interfaceClass))
                .collect(Collectors.toSet());

        Preconditions.checkState(!implClassesEntries.isEmpty(), "Can't find implementation for class: " + interfaceClass);

        // If qualifier not null return needed implementation instance
        // Otherwise return first implementation
        if (qualifier != null) {
            return implClassesEntries.stream()
                    .filter(e -> e.getKey().getSimpleName().equals(qualifier))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new IllegalStateException("Can't find implementation class for qualifier: " + qualifier));
        }
        else return implClassesEntries.stream()
                .findFirst()
                .get()
                .getKey();
    }
}
