package com.ivchenko.demo.service;

import com.google.common.collect.Lists;
import com.ivchenko.demo.model.User;
import com.ivchenko.ioc.annotation.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserServiceImpl2 implements UserService {
    private List<User> users = Lists.newArrayList();

    @Override
    public List<User> getAllUsers() {
        System.out.println("Hello, Dependency Injection");
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public void addUser(User user) {
        users.add(user);
    }

    @Override
    public void removeUser(User user) {
        users.remove(user);
    }
}
