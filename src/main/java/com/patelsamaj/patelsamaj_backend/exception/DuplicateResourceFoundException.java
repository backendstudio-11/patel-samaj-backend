package com.patelsamaj.patelsamaj_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateResourceFoundException extends RuntimeException {
    public DuplicateResourceFoundException(String message){
        super(message);
    }
}
