package com.ivchenko.ioc.services;

import com.ivchenko.ioc.annotation.Component;

@Component
public class AccountServiceImpl implements AccountService {
    @Override
    public Long getAccountNumber(String userName) {
        return 123L;
    }
}
