package com.ivchenko.ioc.services;

import com.ivchenko.ioc.annotation.Component;

@Component
public class AccountImpl2 implements AccountService {
    @Override
    public Long getAccountNumber(String userName) {
        return 1234L;
    }
}
