package com.ivchenko.ioc.application;

import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.annotation.PostConstructor;

@Component
public class Class2 {
    private Class3 class3;
    private UserApplication userApplication;

    @Autowired
    public Class2(Class3 class3, UserApplication userApplication) {
        this.class3 = class3;
        this.userApplication = userApplication;
    }

    @PostConstructor
    public void postConstructor() {
        System.out.println(class3.toString());
        userApplication.displayInfo();
    }

    @Override
    public String toString() {
        return "hello form class 2";
    }
}
