package br.com.agendamento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @NotNull(message = "O cliente é obrigatório.")
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    @NotNull(message = "O profissional é obrigatório.")
    private Usuario profissional;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "agendamento_servicos",
            joinColumns = @JoinColumn(name = "agendamento_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    @NotNull(message = "Pelo menos um serviço deve ser selecionado.")
    @Size(min = 1, message = "Selecione pelo menos um serviço.")
    private List<Servico> servicos;

    @NotNull(message = "A data e hora de início são obrigatórias.")
    @Future(message = "A data e hora do agendamento devem estar no futuro.")
    private LocalDateTime dataHora;

    private LocalDateTime dataHoraFim;

    private String status;
}