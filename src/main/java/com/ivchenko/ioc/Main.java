package com.ivchenko.ioc;

import com.ivchenko.ioc.injector.Injector;

public class Main {
    public static void main(String[] args) {
        Injector.startApplication(Main.class);
    }
}