package br.com.agendamento.api.dto;

import br.com.agendamento.api.model.Agendamento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class DadosDetalhamentoAgendamentoDTO {

    private UUID idAgendamento;
    private UUID idCliente;
    private String nomeCliente;
    private UUID idProfissional;
    private String nomeProfissional;
    private List<String> servicos;
    private LocalDateTime dataHora;
    private String status;

    public DadosDetalhamentoAgendamentoDTO() {
    }

    public DadosDetalhamentoAgendamentoDTO(Agendamento agendamento) {
        this.idAgendamento = agendamento.getId();
        this.idCliente = agendamento.getCliente().getId();
        this.nomeCliente = agendamento.getCliente().getNome();
        this.idProfissional = agendamento.getProfissional().getId();
        this.nomeProfissional = agendamento.getProfissional().getNome();
        this.servicos = agendamento.getServicos().stream()
                .map(s -> s.getNome())
                .collect(Collectors.toList());
        this.dataHora = agendamento.getDataHora();
        this.status = agendamento.getStatus();
    }
}