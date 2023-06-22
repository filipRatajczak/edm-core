package com.edm.edmcore.client;


import com.edm.edmcore.model.DispositionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Component
public class DispositionClient {

    private final WebClient webClient;

    public DispositionClient(@Value("${edm.api}") String apiUrl) {
        this.webClient = WebClient.create(apiUrl);
    }

    public List<DispositionDto> getAllDisposition() {
        Mono<List<DispositionDto>> disposition = webClient.get()
                .uri("/api/v1/dispositions")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        return disposition.block();
    }

    public List<DispositionDto> getAllDispositionByEmployeeCode(String employeeCode, LocalDate from, LocalDate to) {

        String uri = UriComponentsBuilder.newInstance().path("/api/v1/dispositions/" + employeeCode)
                .query("from={from}").query("to={to}").buildAndExpand(from.toString(), to.toString()).toString();

        Mono<List<DispositionDto>> disposition = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

        return disposition.block();
    }


}
