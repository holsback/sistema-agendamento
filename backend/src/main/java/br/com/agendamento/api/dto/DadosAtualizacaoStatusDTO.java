package br.com.agendamento.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DadosAtualizacaoStatusDTO {
    @NotBlank
    @Pattern(regexp = "^(Pendente|Confirmado|Concluído|Cancelado)$", message = "Status inválido.")
    private String status;
}