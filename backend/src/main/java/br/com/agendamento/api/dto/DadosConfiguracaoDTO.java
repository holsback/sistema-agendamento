package br.com.agendamento.api.dto;

import br.com.agendamento.api.model.Configuracao;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class DadosConfiguracaoDTO {
    private LocalTime inicioExpediente;
    private LocalTime fimExpediente;
    private Set<DayOfWeek> diasFuncionamento;

    public DadosConfiguracaoDTO(Configuracao config) {
        this.inicioExpediente = config.getInicioExpediente();
        this.fimExpediente = config.getFimExpediente();
        this.diasFuncionamento = config.getDiasFuncionamento();
    }
}