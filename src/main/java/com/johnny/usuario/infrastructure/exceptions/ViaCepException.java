package com.johnny.usuario.infrastructure.exceptions;

public class ViaCepException extends RuntimeException {

    public ViaCepException(String mensagem) {
        super(mensagem);
    }

    public ViaCepException(String mensagem, Throwable throwable) {
        super(mensagem, throwable);
    }
}