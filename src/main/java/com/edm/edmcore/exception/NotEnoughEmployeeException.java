package com.edm.edmcore.exception;

import com.edm.edmcore.model.DispositionDto;
import lombok.Getter;

import java.util.Set;

@Getter
public class NotEnoughEmployeeException extends RuntimeException {

    private Set<DispositionDto> dispositionDtos;

    public NotEnoughEmployeeException(String message, Set<DispositionDto> dispositionDtos) {
        super(message);
        this.dispositionDtos = dispositionDtos;
    }
}
