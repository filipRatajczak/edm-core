package com.edm.edmcore.register;

import com.edm.edmcore.client.UserClient;
import com.edm.edmcore.model.AuthenticationToken;
import com.edm.edmcore.model.AuthenticationTokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthTokenScheduler {

    private final UserClient userClient;

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 5)
    public void fetchAuthToken() {

        log.info("Sending request for token. Next request will be sent in 5 minutes.");

        AuthenticationToken authToken = userClient.getAuthToken();

        AuthenticationTokenHolder.jwtToken = authToken.getToken();

        log.info("Token successfully retrieved. Next request will be sent in 5 minutes. Token:[{}]", AuthenticationTokenHolder.jwtToken);

    }

}
