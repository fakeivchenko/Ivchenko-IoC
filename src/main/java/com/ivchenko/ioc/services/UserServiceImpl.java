package com.ivchenko.ioc.services;

import com.ivchenko.ioc.annotation.Component;

@Component
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName() {
        return "fakeivchenko";
    }
}
