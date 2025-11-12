package br.com.agendamento.api.controller;

import br.com.agendamento.api.exception.ResourceNotFoundException;
import br.com.agendamento.api.model.Servico;
import br.com.agendamento.api.repository.ServicoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    @Autowired
    private ServicoRepository servicoRepository;

    /**
     * Lista apenas os serviços ATIVOS
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_PROFISSIONAL', 'ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<List<Servico>> listar() {
        return ResponseEntity.ok(servicoRepository.findAllByAtivoTrue());
    }

    /**
     * Cria um novo serviço
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<Servico> criar(@RequestBody @Valid Servico servico) {
        servico.setAtivo(true); // Garante que ao criar, ele está ativo
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoRepository.save(servico));
    }

    /**
     * Atualiza um serviço existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_GERENTE', 'ROLE_DONO', 'ROLE_MASTER')")
    public ResponseEntity<Servico> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid Servico dadosAtualizados
    ) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + id + " não encontrado."));

        servico.setNome(dadosAtualizados.getNome());
        servico.setDescricao(dadosAtualizados.getDescricao());
        servico.setPreco(dadosAtualizados.getPreco());
        servico.setDuracaoMinutos(dadosAtualizados.getDuracaoMinutos());
        servico.setAtivo(dadosAtualizados.getAtivo()); // Permite reativar um serviço

        return ResponseEntity.ok(servicoRepository.save(servico));
    }

    /**
     * Desativa (Soft Delete) um serviço
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_DONO', 'ROLE_MASTER', 'ROLE_GERENTE')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        servico.setAtivo(false); // Apenas "desliga" o serviço
        servicoRepository.save(servico);

        return ResponseEntity.noContent().build();
    }
}