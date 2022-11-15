package com.ivchenko.ioc.application;

import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.annotation.PostConstructor;

public abstract class ClassA {
    private UserApplication userApplication;

    @Autowired
    public ClassA(UserApplication userApplication) {
        this.userApplication = userApplication;
    }

    @PostConstructor
    public abstract void post();
}
