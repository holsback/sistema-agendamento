package br.com.agendamento.api.dto;

import br.com.agendamento.api.model.enums.PerfilUsuario;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DadosAtualizacaoColaboradorDTO {

    private String nome;

    @Pattern(
            regexp = "^(\\(\\d{2}\\)\\s?)?(\\d{4,5}-?\\d{4})$",
            message = "Formato de telefone inválido."
    )
    private String telefone;

    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres.")
    private String senha;

    private PerfilUsuario perfil;
}