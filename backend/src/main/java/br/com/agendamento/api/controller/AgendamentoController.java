package br.com.agendamento.api.controller;

// (Todos os imports... java.util.List, java.util.UUID, etc.)
import br.com.agendamento.api.dto.DadosCriacaoAgendamentoDTO;
import br.com.agendamento.api.dto.DadosAtualizacaoStatusDTO;
import br.com.agendamento.api.dto.DadosDetalhamentoAgendamentoDTO;
import br.com.agendamento.api.exception.ConflitoDeHorarioException;
import br.com.agendamento.api.exception.ResourceNotFoundException;
import br.com.agendamento.api.model.Agendamento;
import br.com.agendamento.api.model.Configuracao;
import br.com.agendamento.api.model.Servico;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import br.com.agendamento.api.repository.AgendamentoRepository;
import br.com.agendamento.api.repository.ConfiguracaoRepository;
import br.com.agendamento.api.repository.ServicoRepository;
import br.com.agendamento.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    // (Todos os @Autowired... AgendamentoRepository, UsuarioRepository, etc.)
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private ConfiguracaoRepository configuracaoRepository;


    // (O método criar())
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<DadosDetalhamentoAgendamentoDTO> criar(
            @RequestBody @Valid DadosCriacaoAgendamentoDTO dados,
            Authentication authentication
    ) {
        Configuracao config = configuracaoRepository.findAll().get(0);
        LocalDateTime dataHoraInicio = dados.getDataHora();

        if (!config.getDiasFuncionamento().contains(dataHoraInicio.getDayOfWeek())) {
            throw new ConflitoDeHorarioException("O estabelecimento não funciona neste dia da semana.");
        }
        if (dataHoraInicio.toLocalTime().isBefore(config.getInicioExpediente()) ||
                dataHoraInicio.toLocalTime().isAfter(config.getFimExpediente())) {
            throw new ConflitoDeHorarioException("Fora do horário de funcionamento.");
        }
        String emailDono = authentication.getName();
        Usuario cliente = (Usuario) usuarioRepository.findByEmail(emailDono)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado."));
        Usuario profissional = usuarioRepository.findById(dados.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado."));
        List<Servico> servicosSelecionados = servicoRepository.findAllById(dados.getServicosIds());
        if (servicosSelecionados.isEmpty() || servicosSelecionados.size() < dados.getServicosIds().size()) {
            throw new ResourceNotFoundException("Um ou mais serviços selecionados não foram encontrados.");
        }
        int duracaoTotal = servicosSelecionados.stream().mapToInt(Servico::getDuracaoMinutos).sum();
        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(duracaoTotal);
        if (dataHoraFim.toLocalTime().isAfter(config.getFimExpediente())) {
            throw new ConflitoDeHorarioException("Este agendamento (duração: " + duracaoTotal + " min) terminaria após o fechamento.");
        }
        if (agendamentoRepository.existeConflitoDeHorario(profissional, dataHoraInicio, dataHoraFim, null)) {
            throw new ConflitoDeHorarioException("Conflito: O profissional já está ocupado neste horário.");
        }
        Agendamento novoAgendamento = new Agendamento();
        novoAgendamento.setCliente(cliente);
        novoAgendamento.setProfissional(profissional);
        novoAgendamento.setServicos(servicosSelecionados);
        novoAgendamento.setDataHora(dataHoraInicio);
        novoAgendamento.setDataHoraFim(dataHoraFim);
        novoAgendamento.setStatus("Pendente");
        agendamentoRepository.save(novoAgendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DadosDetalhamentoAgendamentoDTO(novoAgendamento));
    }

    /**
     * (CORREÇÃO!) Lista os agendamentos (filtrados por perfil e ordenados por data)
     * REVERTEMOS esta função. Ela agora envia TODOS os status (incluindo Cancelado),
     * pois as listas de gerenciamento precisam deles.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_PROFISSIONAL', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<List<DadosDetalhamentoAgendamentoDTO>> listar(Authentication authentication) {
        Usuario usuarioLogado = (Usuario) usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado no token."));

        List<Agendamento> agendamentosSujos;

        // Busca ordenada por data
        if (usuarioLogado.getPerfil() == PerfilUsuario.ROLE_CLIENTE) {
            agendamentosSujos = agendamentoRepository.findByClienteOrderByDataHoraAsc(usuarioLogado);
        } else if (usuarioLogado.getPerfil() == PerfilUsuario.ROLE_PROFISSIONAL) {
            agendamentosSujos = agendamentoRepository.findByProfissionalOrderByDataHoraAsc(usuarioLogado);
        } else {
            agendamentosSujos = agendamentoRepository.findAllByOrderByDataHoraAsc();
        }

        // Converte para DTO (NÃO FILTRA AQUI)
        List<DadosDetalhamentoAgendamentoDTO> listaLimpa = agendamentosSujos.stream()
                .map(DadosDetalhamentoAgendamentoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(listaLimpa);
    }

    // (O método atualizar())
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER') or @securityService.isAgendamentoOwner(authentication, #id)")
    public ResponseEntity<DadosDetalhamentoAgendamentoDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid DadosCriacaoAgendamentoDTO dados,
            Authentication authentication
    ) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        Usuario profissional = usuarioRepository.findById(dados.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado."));
        List<Servico> servicosSelecionados = servicoRepository.findAllById(dados.getServicosIds());
        if (servicosSelecionados.size() < dados.getServicosIds().size()) {
            throw new ResourceNotFoundException("Um ou mais serviços não encontrados.");
        }
        int duracaoTotal = servicosSelecionados.stream().mapToInt(Servico::getDuracaoMinutos).sum();
        LocalDateTime novoInicio = dados.getDataHora();
        LocalDateTime novoFim = novoInicio.plusMinutes(duracaoTotal);
        if (agendamentoRepository.existeConflitoDeHorario(profissional, novoInicio, novoFim, id)) {
            throw new ConflitoDeHorarioException("Conflito: O profissional já está ocupado neste novo horário.");
        }
        agendamento.setProfissional(profissional);
        agendamento.setServicos(servicosSelecionados);
        agendamento.setDataHora(novoInicio);
        agendamento.setDataHoraFim(novoFim);
        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);
        return ResponseEntity.ok(new DadosDetalhamentoAgendamentoDTO(agendamentoSalvo));
    }

    // (O método deletar())
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER') or @securityService.isAgendamentoOwner(authentication, #id)")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        agendamentoRepository.delete(agendamento);
        return ResponseEntity.noContent().build();
    }

    // (O método atualizarStatus())
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_PROFISSIONAL', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER') or @securityService.isAgendamentoOwner(authentication, #id)")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid DadosAtualizacaoStatusDTO dados,
            Authentication authentication
    ) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
        Usuario usuarioLogado = (Usuario) usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado no token."));
        if (usuarioLogado.getPerfil() == PerfilUsuario.ROLE_PROFISSIONAL) {
            if (!agendamento.getProfissional().getId().equals(usuarioLogado.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        agendamento.setStatus(dados.getStatus());
        agendamentoRepository.save(agendamento);
        return ResponseEntity.noContent().build();
    }
}