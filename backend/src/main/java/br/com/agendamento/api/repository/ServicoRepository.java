package br.com.agendamento.api.repository;

import br.com.agendamento.api.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    List<Servico> findAllByAtivoTrue();
}