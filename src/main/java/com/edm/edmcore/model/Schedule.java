package com.edm.edmcore.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "edm-core")
public class Schedule {

    @Id
    private UUID id = UUID.randomUUID();
    private LocalDate day;
    private Set<DispositionDto> dispositionDtos;
    private String errorMessage;

    public Schedule(LocalDate day, Set<DispositionDto> dispositionDtos, String errorMessage) {
        this.day = day;
        this.dispositionDtos = dispositionDtos;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", localDate=" + day +
                ", dispositionDtos=" + dispositionDtos +
                ", errorMessages=" + errorMessage +
                '}';
    }
}
