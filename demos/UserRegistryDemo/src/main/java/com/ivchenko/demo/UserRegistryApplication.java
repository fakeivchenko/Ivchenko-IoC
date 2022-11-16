package com.ivchenko.demo;

import com.ivchenko.demo.model.User;
import com.ivchenko.demo.service.UserService;
import com.ivchenko.ioc.annotation.Autowired;
import com.ivchenko.ioc.annotation.Component;
import com.ivchenko.ioc.annotation.PostConstructor;
import com.ivchenko.ioc.annotation.Qualifier;
import com.ivchenko.ioc.injector.Injector;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class UserRegistryApplication {
    private UserService userService;

    @Autowired
    public UserRegistryApplication(@Qualifier("UserServiceImpl2") UserService userService) {
        this.userService = userService;
    }

    @PostConstructor
    public void displayAllUsers() {
        System.out.println("Original user list: ");
        userService.getAllUsers().forEach(System.out::println);
        System.out.println("--------------------");

        userService.addUser(
                User.builder()
                        .id(3L)
                        .firstName("Paul")
                        .lastName("Strong")
                        .dob(LocalDate.of(2006, 8, 20))
                        .build()
        );

        System.out.println("User list with added user: ");
        userService.getAllUsers().forEach(System.out::println);
        System.out.println("--------------------");


        Optional<User> userById = userService.getUserById(3L);
        userById.ifPresentOrElse(user -> userService.removeUser(user), () -> {
            throw new IllegalStateException("User cannot be found");
        });

        System.out.println("User list with removed user: ");
        userService.getAllUsers().forEach(System.out::println);
        System.out.println("--------------------");
    }

    public static void main(String[] args) {
        Injector.startApplication(UserRegistryApplication.class);
    }
}
