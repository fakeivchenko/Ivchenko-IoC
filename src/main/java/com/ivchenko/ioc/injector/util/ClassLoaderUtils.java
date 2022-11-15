package com.ivchenko.ioc.injector.util;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.Set;

/**
 * Utility class used to make work with ClassLoader more convenient
 * @implNote Implemented used Google Guava
 */
public class ClassLoaderUtils {
    /**
     * Returns classes for the specified package
     * @param packageName name of the package from which the classes will be read
     * @param includeNested returned Set will contain nested classes or not
     * @return all classes in package
     * @throws IOException if the attempt to read class path resources failed
     */
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

    /**
     * Overrides {@link ClassLoaderUtils#getClassesInPackage(String, boolean)}.
     * Always returns with nested classes
     * @param packageName name of the package from which the classes will be read
     * @return all classes in package (including nested classes)
     * @throws IOException if the attempt to read class path resources failed
     */
    public static Set<Class<?>> getClassesInPackage(String packageName) throws IOException {
        return getClassesInPackage(packageName, true);
    }
}
