package com.ivchenko.ioc.injector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.injector.util.InjectionUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ivchenko.ioc.injector.util.ClassLoaderUtils.getClassesInPackage;
import static com.ivchenko.ioc.injector.util.InjectionUtils.getImplementationClass;
import static com.ivchenko.ioc.injector.util.ReflectionUtils.*;

public class Injector {
    // Key: Implementation class
    // Value: Implemented interface
    private Map<Class<?>, Class<?>> injectionMap;

    private Map<Class<?>, Object> applicationScope;

    private Map<Class<?>, ComponentStatus> componentsStatusMap; // Can be replaced with observer =\

    private Injector() {
        injectionMap = Maps.newHashMap();
        applicationScope = Maps.newHashMap();
        componentsStatusMap = Maps.newHashMap();
    }

    public static void startApplication(Class<?> mainClass) {
        getInstance().initialize(mainClass);
    }

    @SneakyThrows
    private void initialize(Class<?> mainClass) {
        Set<Class<?>> packageClasses = getClassesInPackage(mainClass.getPackageName());
        Set<Class<?>> componentClasses = getClassesAnnotatedWith(Component.class, packageClasses);

        // Populating diMap
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

    private Object createInstance(Class<?> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        checkCreationPreconditions(clazz);
        componentsStatusMap.put(clazz, ComponentStatus.CREATING);
        // for not autowired with no args constructor
        if (!hasAutowiredConstructor(clazz) && hasNoArgsConstructor(clazz)) {
            Object instance = clazz.getConstructor().newInstance();
            componentsStatusMap.put(clazz, ComponentStatus.CREATED);
            return instance;
        }
        // for not autowired without no args constructor
        if (!hasAutowiredConstructor(clazz) && !hasNoArgsConstructor(clazz)) {
            componentsStatusMap.put(clazz, ComponentStatus.CREATED);
            return null;
        }
        // for autowired
        if (hasAutowiredConstructor(clazz)) {
            Set<Constructor<?>> autowiredConstructors = getAutowiredConstructors(clazz);
            Preconditions.checkState(
                    autowiredConstructors.size() == 1,
                    "Too many autowired constructors: " + clazz.getName());
            Constructor<?> constructor = autowiredConstructors.stream().findFirst().get();
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            List<Object> parameterInstances = Lists.newArrayList();
            for (Class<?> pt : parameterTypes) {
                Object beanInstance = getBeanInstance(pt);
                if (beanInstance == null) {
                    Class<?> implClass = getImplementationClass(pt, injectionMap);
                    beanInstance = createInstance(implClass);
                    applicationScope.put(implClass, beanInstance);
                }
                parameterInstances.add(beanInstance);
            }
            Object instance = constructor.newInstance(parameterInstances.toArray());
            componentsStatusMap.put(clazz, ComponentStatus.CREATED);

            for (Method m : getPostConstructorMethods(clazz)) {
                m.invoke(instance);
            }

            return instance;
        }
        return null;
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
        return InjectionUtils.getBeanInstance(interfaceClass, injectionMap, applicationScope);
    }

    private static Injector getInstance() {
        return InjectorHolder.INSTANCE;
    }

    private static class InjectorHolder {
        private static final Injector INSTANCE = new Injector();
    }

    private enum ComponentStatus {
        NOT_CREATED, CREATING, CREATED
    }
}
