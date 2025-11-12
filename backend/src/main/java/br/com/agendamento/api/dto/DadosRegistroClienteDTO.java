package br.com.agendamento.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DadosRegistroClienteDTO {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @NotBlank(message = "O email é obrigatório.")
    @Email(message = "Formato de email inválido.")
    private String email;

    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern(
            regexp = "^(\\(\\d{2}\\)\\s?)?(\\d{4,5}-?\\d{4})$",
            message = "Formato de telefone inválido."
    )
    private String telefone;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    private String senha;

    // Qualquer um que se registrar por este "Formulário"
    // será automaticamente um "ROLE_CLIENTE".
}