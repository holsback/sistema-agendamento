package br.com.agendamento.api.controller;

import br.com.agendamento.api.dto.DadosDetalhamentoColaboradorDTO;
import br.com.agendamento.api.exception.ResourceNotFoundException;
import br.com.agendamento.api.model.Agendamento;
import br.com.agendamento.api.model.Configuracao;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import br.com.agendamento.api.repository.AgendamentoRepository;
import br.com.agendamento.api.repository.ConfiguracaoRepository;
import br.com.agendamento.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    /**
     * Lista todos os usuários com perfil PROFISSIONAL (ativos).
     */
    @GetMapping("/profissionais")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<List<DadosDetalhamentoColaboradorDTO>> listarProfissionais() {
        List<Usuario> listaSuja = usuarioRepository.findByPerfil(PerfilUsuario.ROLE_PROFISSIONAL);
        List<DadosDetalhamentoColaboradorDTO> listaLimpa = listaSuja.stream()
                .map(DadosDetalhamentoColaboradorDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(listaLimpa);
    }

    /**
     * Calcula e retorna uma lista de horários de início disponíveis para um profissional.
     */
    @GetMapping("/{idProfissional}/disponibilidade")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<List<LocalTime>> getHorariosDisponiveis(
            @PathVariable UUID idProfissional,
            @RequestParam("data") LocalDate data,
            @RequestParam("duracao") Integer duracaoServico) {

        Configuracao config = configuracaoRepository.findAll().get(0);
        LocalTime inicioExpediente = config.getInicioExpediente();
        LocalTime fimExpediente = config.getFimExpediente();

        if (!config.getDiasFuncionamento().contains(data.getDayOfWeek())) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        Usuario profissional = usuarioRepository.findById(idProfissional)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.plusDays(1).atStartOfDay();

        // 1. Busca TODOS os agendamentos do dia
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findAgendamentosDoDia(profissional, inicioDoDia, fimDoDia);

        // 2. Filtra a lista para conter APENAS os que estão "Pendentes" (ou "Confirmados", se usarmos)
        // Ignoramos agendamentos "Cancelado" ou "Concluído"
        List<Agendamento> agendamentosAtivos = agendamentosDoDia.stream()
                .filter(ag -> ag.getStatus().equals("Pendente"))
                .collect(Collectors.toList());

        List<LocalTime> horariosLivres = new ArrayList<>();
        int intervaloMinutos = 15;
        LocalTime slotAtual = inicioExpediente;

        while (slotAtual.isBefore(fimExpediente)) {
            LocalDateTime inicioSlot = data.atTime(slotAtual);
            LocalDateTime fimSlot = inicioSlot.plusMinutes(duracaoServico);

            if (fimSlot.toLocalTime().isAfter(fimExpediente)) {
                break;
            }
            if (inicioSlot.isBefore(LocalDateTime.now())) {
                slotAtual = slotAtual.plusMinutes(intervaloMinutos);
                continue;
            }

            // 3. Usa a lista FILTRADA (agendamentosAtivos) para checar conflitos
            boolean temConflito = false;
            for (Agendamento ag : agendamentosAtivos) {
                if (inicioSlot.isBefore(ag.getDataHoraFim()) && fimSlot.isAfter(ag.getDataHora())) {
                    temConflito = true;
                    break;
                }
            }
            if (!temConflito) {
                horariosLivres.add(slotAtual);
            }
            slotAtual = slotAtual.plusMinutes(intervaloMinutos);
        }
        return ResponseEntity.ok(horariosLivres);
    }
}