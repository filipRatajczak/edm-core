package com.edm.edmcore.controller;


import com.edm.edmcore.model.AuthenticationToken;
import com.edm.edmcore.model.EmployeeCredentials;
import com.edm.edmcore.model.Role;
import com.edm.edmcore.security.JwtUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<AuthenticationToken> loginGet(@RequestBody EmployeeCredentials employeeCredentials) {

        AuthenticationToken authenticationToken = new AuthenticationToken();
        if (employeeCredentials.getEmail().equals(email) && employeeCredentials.getPassword().equals(password)) {
            String jwtToken = jwtUtilities.generateToken(employeeCredentials.getEmail(), List.of(Role.INTERNAL.name()));
            authenticationToken.setToken(jwtToken);
            authenticationToken.setRole(Role.INTERNAL);
            authenticationToken.setEmail(employeeCredentials.getEmail());
            authenticationToken.setEmployeeCode(null);
        } else {
            throw new UsernameNotFoundException("Employee with email: " + employeeCredentials.getEmail() + " not found.");
        }

        return ResponseEntity.ok(authenticationToken);

    }

}
