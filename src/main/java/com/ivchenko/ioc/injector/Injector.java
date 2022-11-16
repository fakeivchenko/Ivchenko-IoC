package com.ivchenko.ioc.injector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.injector.util.InjectionUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.ivchenko.ioc.injector.util.ClassLoaderUtils.getClassesInPackage;
import static com.ivchenko.ioc.injector.util.InjectionUtils.getImplementationClass;
import static com.ivchenko.ioc.injector.util.ReflectionUtils.*;

/**
 * Injector, creates objects for {@link Component} classes, injects all dependencies
 */
public class Injector {
    // Key: Implementation class
    // Value: Implemented interface
    private Map<Class<?>, Class<?>> injectionMap;
    // Key: Instance class
    // Value: Instance
    private Map<Class<?>, Object> applicationScope;

    private Map<Class<?>, ComponentStatus> componentsStatusMap; // Can be replaced with observer =\

    private Injector() {
        injectionMap = Maps.newHashMap();
        applicationScope = Maps.newHashMap();
        componentsStatusMap = Maps.newHashMap();
    }

    /**
     * Initializes injector and starts application
     * @param mainClass main application class
     */
    public static void startApplication(Class<?> mainClass) {
        getInstance().initialize(mainClass);
    }

    @SneakyThrows
    private void initialize(Class<?> mainClass) {
        Set<Class<?>> packageClasses = getClassesInPackage(mainClass.getPackageName());
        Set<Class<?>> componentClasses = getClassesAnnotatedWith(Component.class, packageClasses);

        // Populating injectionMap
        for (Class<?> c : componentClasses) {
            Class<?>[] interfaces = c.getInterfaces();
            if (interfaces.length == 0)
                injectionMap.put(c, c);
            else for (Class<?> i : interfaces)
                injectionMap.put(c, i);
        }

        componentClasses.forEach(c -> componentsStatusMap.put(c, ComponentStatus.NOT_CREATED));
        // Creating instances
        for (Class<?> c : componentClasses) {
            if (!applicationScope.containsKey(c))
                applicationScope.put(c, createInstance(c));
        }
    }

    private Object createInstance(Class<?> clazz) {
        checkCreationPreconditions(clazz);
        componentsStatusMap.put(clazz, ComponentStatus.CREATING);
        // for not autowired with no args constructor
        if (!hasAutowiredConstructor(clazz) && hasNoArgsConstructor(clazz)) {
            return createInstanceNoArgs(clazz);
        }
        // for not autowired without no args constructor
        if (!hasAutowiredConstructor(clazz) && !hasNoArgsConstructor(clazz)) {
            return createInstanceFirstConstructor(clazz);
        }
        // for autowired
        if (hasAutowiredConstructor(clazz)) {
            return createInstanceAutowired(clazz);
        }
        // Should not happen
        throw new IllegalStateException("Unknown error while creating instance for class: " + clazz);
    }

    @SneakyThrows
    private Object createInstanceNoArgs(Class<?> clazz) {
        Object instance = clazz.getConstructor().newInstance();
        componentsStatusMap.put(clazz, ComponentStatus.CREATED);
        return instance;
    }

    @SneakyThrows
    private Object createInstanceFirstConstructor(Class<?> clazz) {
        Set<Constructor<?>> constructors = getAllConstructors(clazz);
        Preconditions.checkState(
                constructors.size() > 0,
                "No public constructors declared: " + clazz
        );
        Constructor<?> constructor = constructors.stream()
                .findFirst()
                .get();
        List<Object> parametersInstances = getConstructorParametersInstances(constructor);
        Object instance = constructor.newInstance(parametersInstances.toArray());
        componentsStatusMap.put(clazz, ComponentStatus.CREATED);

        invokePostConstructor(instance);
        return instance;
    }

    @SneakyThrows
    private Object createInstanceAutowired(Class<?> clazz) {
        Set<Constructor<?>> autowiredConstructors = getAutowiredConstructors(clazz);
        Preconditions.checkState(
                autowiredConstructors.size() == 1,
                "Too many autowired constructors: " + clazz.getName());
        Constructor<?> constructor = autowiredConstructors.stream()
                .findFirst()
                .get();
        List<Object> parametersInstances = getConstructorParametersInstances(constructor);
        Object instance = constructor.newInstance(parametersInstances.toArray());
        componentsStatusMap.put(clazz, ComponentStatus.CREATED);

        invokePostConstructor(instance);
        return instance;
    }

    private List<Object> getConstructorParametersInstances(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        List<Object> parameterInstances = Lists.newArrayList();

        for (Parameter p : parameters) {
            Class<?> pType = p.getType();
            String qualifier = getQualifierValue(p);

            Object beanInstance = getBeanInstance(pType, qualifier);
            if (beanInstance == null) {
                Class<?> implClass = getImplementationClass(pType, injectionMap, qualifier);
                beanInstance = createInstance(implClass);
                applicationScope.put(implClass, beanInstance);
            }
            parameterInstances.add(beanInstance);
        }
        return parameterInstances;
    }

    @SneakyThrows
    private void invokePostConstructor(Object instance) {
        Set<Method> postConstructorMethods = getPostConstructorMethods(instance.getClass());
        Preconditions.checkState(
                postConstructorMethods.size() <= 1,
                "Too many PostConstructor methods: " + instance.getClass().getName()
        );
        Optional<Method> method = postConstructorMethods.stream()
                .findFirst();
        if (method.isEmpty()) return;

        Preconditions.checkState(
                method.get().getParameterCount() == 0,
                "Invalid PostConstructor parameters count: " + method.get()
        );
        method.get().invoke(instance);
    }

    private void checkCreationPreconditions(Class<?> clazz) {
        Preconditions.checkState(
                componentsStatusMap.get(clazz) != ComponentStatus.CREATING,
                "Requested bean is already in creation: Maybe there an unresolvable circular reference"
        );
        Preconditions.checkState(
                componentsStatusMap.get(clazz) != ComponentStatus.CREATED,
                "Requested bean has been already created"
        );
    }

    private <T> T getBeanInstance(Class<T> interfaceClass) {
        return getBeanInstance(interfaceClass, null);
    }

    private <T> T getBeanInstance(Class<T> interfaceClass, String qualifier) {
        return InjectionUtils.getBeanInstance(interfaceClass, injectionMap, applicationScope, qualifier);
    }

    private static Injector getInstance() {
        return InjectorHolder.INSTANCE;
    }

    /**
     * Holds Injector instance (Singleton implementation)
     */
    private static class InjectorHolder {
        private static final Injector INSTANCE = new Injector();
    }

    private enum ComponentStatus {
        NOT_CREATED, CREATING, CREATED
    }
}
