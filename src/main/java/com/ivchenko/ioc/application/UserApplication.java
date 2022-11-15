package com.ivchenko.ioc.application;

import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.annotation.PostConstructor;
import com.ivchenko.ioc.services.AccountService;
import com.ivchenko.ioc.services.UserService;

@Component
public class UserApplication {
    private UserService userService;
    private AccountService accountService;

    @Autowired
    public UserApplication(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostConstructor
    public void displayInfo() {
        System.out.println(userService.getUserName() + " : " + accountService.getAccountNumber(userService.getUserName()));
    }
}
