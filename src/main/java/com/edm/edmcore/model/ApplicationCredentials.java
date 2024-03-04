package com.edm.edmcore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class ApplicationCredentials {

    @Value("${edm.core.credentials.email}")
    private String email;
    @Value("${edm.core.credentials.password}")
    private String password;

}
