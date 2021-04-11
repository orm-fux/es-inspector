package com.github.ormfux.esi.exception;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
