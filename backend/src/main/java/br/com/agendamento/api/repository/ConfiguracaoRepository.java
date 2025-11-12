package br.com.agendamento.api.repository;

import br.com.agendamento.api.model.Configuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConfiguracaoRepository extends JpaRepository<Configuracao, UUID> {
}