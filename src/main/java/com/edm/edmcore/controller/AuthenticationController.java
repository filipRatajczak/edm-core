package com.edm.edmcore.controller;


import com.edm.edmcore.model.AuthenticationToken;
import com.edm.edmcore.model.EmployeeCredentialsDto;
import com.edm.edmcore.model.Role;
import com.edm.edmcore.security.JwtUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    @Value("${edm.api.credentials.email}")
    private String email;
    @Value("${edm.api.credentials.password}")
    private String password;
    private final JwtUtilities jwtUtilities;


    @PostMapping("/api/v1/login")
    public ResponseEntity<AuthenticationToken> loginGet(@RequestBody EmployeeCredentialsDto employeeCredentialsDto) {

        AuthenticationToken authenticationToken = new AuthenticationToken();
        if (employeeCredentialsDto.getEmail().equals(email) && employeeCredentialsDto.getPassword().equals(password)) {
            String jwtToken = jwtUtilities.generateToken(employeeCredentialsDto.getEmail(), List.of(Role.INTERNAL.name()));
            authenticationToken.setToken(jwtToken);
            authenticationToken.setRole(Role.INTERNAL);
            authenticationToken.setEmail(employeeCredentialsDto.getEmail());
            authenticationToken.setEmployeeCode(null);
        } else {
            throw new UsernameNotFoundException("Employee with email: " + employeeCredentialsDto.getEmail() + " not found.");
        }

        return ResponseEntity.ok(authenticationToken);

    }

}
