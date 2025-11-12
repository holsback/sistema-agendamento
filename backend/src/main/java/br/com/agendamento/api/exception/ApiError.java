package br.com.agendamento.api.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class ApiError {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private List<String> messages;
    private String path;
    /**
     * Construtor para MÚLTIPLAS mensagens de erro (Validação)
     */
    public ApiError(HttpStatus status, List<String> messages, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.messages = messages;
        this.path = path;
    }

    /**
     * Construtor para UMA ÚNICA mensagem de erro (Ex: Nosso ResourceNotFound)
     * (Isso é uma "sobrecarga" - um construtor com o mesmo nome, mas parâmetros diferentes)
     */
    public ApiError(HttpStatus status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.messages = Collections.singletonList(message);
        this.path = path;
    }
}