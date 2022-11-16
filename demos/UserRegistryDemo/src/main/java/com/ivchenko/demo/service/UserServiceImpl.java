package com.ivchenko.demo.service;

import com.google.common.collect.Lists;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.demo.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {
    private final List<User> users;

    public UserServiceImpl() {
        users = Lists.newArrayList();
        {{
            users.add(
                    User.builder()
                            .id(1L)
                            .firstName("Anton")
                            .lastName("Ivchenko")
                            .dob(LocalDate.of(2002, 6, 8))
                            .build()
            );
            users.add(
                    User.builder()
                            .id(2L)
                            .firstName("Tracy")
                            .lastName("Penn")
                            .dob(LocalDate.of(1990, 2, 10))
                            .build()
            );
        }}
    }

    @Override
    public List<User> getAllUsers() {
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
        // User presence check can be implemented
        users.add(user);
    }

    @Override
    public void removeUser(User user) {
        boolean userPresent = users.stream().anyMatch(u -> u.equals(user));
        if (userPresent) {
            users.remove(user);
        } else throw new IllegalStateException("User " + user + " does not exists");
    }
}
