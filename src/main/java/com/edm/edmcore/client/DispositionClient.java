package com.edm.edmcore.client;


import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.DispositionRatioDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class DispositionClient {

    private final WebClient webClient;

    public DispositionClient(@Value("${edm.api}") String apiUrl) {
        this.webClient = WebClient.create(apiUrl);
    }

    public List<DispositionDto> getAllDisposition(LocalDate from, LocalDate to) {

        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositions")
                .query("from={from}").query("to={to}").buildAndExpand(from.toString(), to.toString()).toString();

        Mono<List<DispositionDto>> disposition = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        return disposition.block();
    }

    public List<DispositionDto> getAllDispositionByEmployeeCode(String employeeCode, LocalDate from, LocalDate to) {

        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositions/" + employeeCode)
                .query("from={from}").query("to={to}").buildAndExpand(from.toString(), to.toString()).toString();

        Flux<List<DispositionDto>> disposition = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {
                });

        return disposition.blockLast();
    }
    public DispositionRatioDto getDispositionRatio(String employeeCode) {

        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositionsRatio/" + employeeCode).build().toUriString();

        return webClient.method(HttpMethod.GET)
                        .uri(uri)
                        .contentType(MediaType.TEXT_PLAIN)
                        .retrieve()
                        .bodyToMono(DispositionRatioDto.class)
                        .block();

    }

}
