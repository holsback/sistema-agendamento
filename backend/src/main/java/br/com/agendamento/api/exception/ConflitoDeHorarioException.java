package br.com.agendamento.api.exception;

public class ConflitoDeHorarioException extends RuntimeException {

    public ConflitoDeHorarioException(String message) {
        super(message);
    }
}