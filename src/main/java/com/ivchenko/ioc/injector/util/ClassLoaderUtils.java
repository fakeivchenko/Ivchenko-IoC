package com.ivchenko.ioc.injector.util;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.Set;

public class ClassLoaderUtils {
    public static Set<Class<?>> getClassesInPackage(String packageName, boolean includeNested) throws IOException {
        Set<Class<?>> classes = Sets.newHashSet();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassPath classPath = ClassPath.from(classLoader);

        if (includeNested) {
            classPath.getAllClasses()
                    .stream()
                    .filter(c -> c.getName().startsWith(packageName))
                    .forEach(c -> classes.add(c.load()));
        } else {
            classPath.getTopLevelClasses(packageName)
                    .forEach(c -> classes.add(c.load()));
        }
        return classes;
    }

    public static Set<Class<?>> getClassesInPackage(String packageName) throws IOException {
        return getClassesInPackage(packageName, true);
    }
}
