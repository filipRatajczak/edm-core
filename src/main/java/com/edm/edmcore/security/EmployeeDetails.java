package com.edm.edmcore.security;

import com.edm.edmcore.model.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class EmployeeDetails implements UserDetails {

    @Value("${edm.api.credentials.email}")
    private String email;
    @Value("${edm.api.credentials.password}")
    private String password;
    private final Role role = Role.INTERNAL;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public EmployeeDetails getEmployeeDetails() {
        EmployeeDetails employeeDetails = new EmployeeDetails();
        employeeDetails.email = this.email;
        employeeDetails.password = this.password;
        return employeeDetails;
    }

}
