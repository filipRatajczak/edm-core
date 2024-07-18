package com.edm.edmcore.client;

import com.edm.edmcore.model.ApplicationCredentials;
import com.edm.edmcore.model.AuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static com.edm.edmcore.util.Constant.CONST_USER_PATH;

@Component
public class UserClient {

    private final WebClient webClient;
    private final ApplicationCredentials applicationCredentials;

    public UserClient(@Value("${edm.api.url}") String apiUrl, ApplicationCredentials applicationCredentials) {
        this.webClient = WebClient.create(apiUrl);
        this.applicationCredentials = applicationCredentials;
    }

    public AuthenticationToken getAuthToken() {

        String uri = UriComponentsBuilder.newInstance().path(CONST_USER_PATH).toUriString();

        Mono<ApplicationCredentials> userCredentialsMono = Mono.just(applicationCredentials);

        Mono<AuthenticationToken> disposition = webClient.post()
                .uri(uri)
                .body(userCredentialsMono, ApplicationCredentials.class)
                .retrieve()
                .bodyToMono(AuthenticationToken.class);

        return disposition.block();
    }


}
