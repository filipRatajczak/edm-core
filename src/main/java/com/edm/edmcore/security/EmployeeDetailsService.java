package com.edm.edmcore.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeDetailsService implements UserDetailsService {

    private final EmployeeDetails employeeDetails;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email.equals(employeeDetails.getEmployeeDetails().getUsername())) {
            return employeeDetails.getEmployeeDetails();
        } else {
            throw new UsernameNotFoundException("Employee with email:  " + email + " not found.");
        }
    }

}
