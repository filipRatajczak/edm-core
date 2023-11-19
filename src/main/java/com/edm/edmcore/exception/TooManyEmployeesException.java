package com.edm.edmcore.exception;

import com.edm.edmcore.model.DispositionDto;
import lombok.Getter;

import java.util.Set;

@Getter
public class TooManyEmployeesException extends RuntimeException {

    private Set<DispositionDto> dispositionDtos;

    public TooManyEmployeesException(String message, Set<DispositionDto> dispositionDtos) {
        super(message);
        this.dispositionDtos = dispositionDtos;
    }
}
