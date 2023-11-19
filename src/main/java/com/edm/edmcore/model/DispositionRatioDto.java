package com.edm.edmcore.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DispositionRatioDto {

    private String employeeCode;
    private BigDecimal ratio;

}
