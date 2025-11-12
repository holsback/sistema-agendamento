package br.com.agendamento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O nome do serviço é obrigatório.")
    @Column(unique = true)
    private String nome;

    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    @NotNull(message = "O preço é obrigatório.")
    @Min(value = 0, message = "O preço não pode ser negativo.")
    private BigDecimal preco;

    @NotNull(message = "A duração é obrigatória.")
    @Min(value = 15, message = "A duração mínima é de 15 minutos.")
    private Integer duracaoMinutos;

    @Column(nullable = false)
    private Boolean ativo = true;
}