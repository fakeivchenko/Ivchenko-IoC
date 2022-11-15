package com.ivchenko.ioc.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data @Builder
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
}
