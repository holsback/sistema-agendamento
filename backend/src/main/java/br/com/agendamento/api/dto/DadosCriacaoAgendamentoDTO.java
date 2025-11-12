package br.com.agendamento.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DadosCriacaoAgendamentoDTO {

    @NotNull(message = "O profissional é obrigatório.")
    private UUID profissionalId;

    @NotEmpty(message = "Selecione pelo menos um serviço.")
    private List<UUID> servicosIds;

    @NotNull(message = "A data e hora são obrigatórias.")
    @Future(message = "A data e hora do agendamento devem estar no futuro.")
    private LocalDateTime dataHora;

}