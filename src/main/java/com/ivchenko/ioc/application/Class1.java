package com.ivchenko.ioc.application;

import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.annotation.PostConstructor;

@Component
public class Class1 {
    private Class2 class2;

    @Autowired
    public Class1(Class2 class2) {
        this.class2 = class2;
    }

    @PostConstructor
    public void postConstructor() {
        System.out.println(class2.toString());
    }

    @Override
    public String toString() {
        return "hello from class 1";
    }
}
