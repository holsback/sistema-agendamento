package br.com.agendamento.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        String errorMessage = ex.getMessage();
        String path = request.getRequestURI();

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                errorMessage,
                path
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<String> errorMessages = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String path = request.getRequestURI();

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                errorMessages,
                path
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {

        String errorMessage = "Corpo da requisição (JSON) inválido ou ilegível. Verifique o formato dos dados.";
        String path = request.getRequestURI();
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                path
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    public ResponseEntity<ApiError> handleConflitoDeHorario(ConflitoDeHorarioException ex, HttpServletRequest request) {

        String errorMessage = ex.getMessage();
        String path = request.getRequestURI();
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                errorMessage,
                path
        );

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }
}