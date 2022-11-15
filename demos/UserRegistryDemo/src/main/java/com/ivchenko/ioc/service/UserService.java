package com.ivchenko.ioc.service;

import com.ivchenko.ioc.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void addUser(User user);

    void removeUser(User user);
}
