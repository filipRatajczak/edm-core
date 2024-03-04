package com.edm.edmcore.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class AuthenticationToken {

    private String email;
    private String token;
    private String employeeCode;
    private Role role;

}
