package com.ivchenko.demo.service;

import com.ivchenko.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void addUser(User user);

    void removeUser(User user);
}
