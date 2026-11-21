package com.movieticketbooking.exception;

public class EmailAlreadyExistsException extends ResourceAlreadyExistException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
