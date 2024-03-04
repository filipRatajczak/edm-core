package com.edm.edmcore.client;


import com.edm.edmcore.model.AuthenticationTokenHolder;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.DispositionRatioDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static com.edm.edmcore.util.Constant.CONST_AUTHORIZATION;
import static com.edm.edmcore.util.Constant.CONST_BEARER;
import static com.edm.edmcore.util.Constant.CONST_DISPOSITION_PATH;

@Component
@Slf4j
public class DispositionClient {

    private final WebClient webClient;

    public DispositionClient(@Value("${edm.api.url}") String apiUrl) {
        this.webClient = WebClient.builder().baseUrl(apiUrl).build();
    }

    public List<DispositionDto> getAllDisposition(LocalDate from, LocalDate to) {


        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositions")
                .query("from={from}").query("to={to}").buildAndExpand(from.toString(), to.toString()).toString();

        Mono<List<DispositionDto>> disposition = webClient.get()
                .uri(uri)
                .header(CONST_AUTHORIZATION, CONST_BEARER + AuthenticationTokenHolder.jwtToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        return disposition.block();
    }

    public List<DispositionDto> getAllDispositionByEmployeeCode(String employeeCode, LocalDate from, LocalDate to) {

        String uri = UriComponentsBuilder.newInstance().path(CONST_DISPOSITION_PATH + employeeCode)
                .query("from={from}").query("to={to}").buildAndExpand(from.toString(), to.toString()).toString();

        Flux<List<DispositionDto>> disposition = webClient.get()
                .uri(uri)
                .header(CONST_AUTHORIZATION, CONST_BEARER + AuthenticationTokenHolder.jwtToken)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });

        return disposition.blockLast();
    }

    public DispositionRatioDto getDispositionRatio(String employeeCode) {

        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositionsRatio/" + employeeCode).build().toUriString();

        return webClient.method(HttpMethod.GET)
                .uri(uri)
                .header(CONST_AUTHORIZATION, CONST_BEARER + AuthenticationTokenHolder.jwtToken)
                .contentType(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(DispositionRatioDto.class)
                .block();

    }

}
