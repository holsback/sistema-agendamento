package br.com.agendamento.api.repository;

import br.com.agendamento.api.model.Agendamento;
import br.com.agendamento.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório para a entidade Agendamento.
 */
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    /**
     * Verifica se existe um conflito de horário (apenas com agendamentos 'Pendentes').
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a " +
            "WHERE a.profissional = :profissional " +
            "AND a.status = 'Pendente' " +
            "AND (a.dataHora < :novoFim AND a.dataHoraFim > :novoInicio) " +
            "AND (:ignorarId IS NULL OR a.id != :ignorarId)")
    boolean existeConflitoDeHorario(
            @Param("profissional") Usuario profissional,
            @Param("novoInicio") LocalDateTime novoInicio,
            @Param("novoFim") LocalDateTime novoFim,
            @Param("ignorarId") UUID ignorarId
    );

    /**
     * Busca todos os agendamentos de um CLIENTE, ordenados por data.
     */
    List<Agendamento> findByClienteOrderByDataHoraAsc(Usuario cliente);

    /**
     * Busca todos os agendamentos de um PROFISSIONAL, ordenados por data.
     */
    List<Agendamento> findByProfissionalOrderByDataHoraAsc(Usuario profissional);

    /**
     * Busca TODOS os agendamentos, ordenados por data.
     */
    List<Agendamento> findAllByOrderByDataHoraAsc();

    /**
     * Busca agendamentos de um profissional em um dia específico (para o 'getHorariosDisponiveis').
     */
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora >= :inicioDoDia AND a.dataHora < :fimDoDia")
    List<Agendamento> findAgendamentosDoDia(
            @Param("profissional") Usuario profissional,
            @Param("inicioDoDia") LocalDateTime inicioDoDia,
            @Param("fimDoDia") LocalDateTime fimDoDia
    );
}