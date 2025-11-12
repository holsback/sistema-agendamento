package br.com.agendamento.api.controller;

import br.com.agendamento.api.dto.DadosConfiguracaoDTO;
import br.com.agendamento.api.model.Configuracao;
import br.com.agendamento.api.repository.ConfiguracaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configuracao")
public class ConfiguracaoController {

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    @GetMapping
    public ResponseEntity<DadosConfiguracaoDTO> buscarConfiguracao() {
        Configuracao config = configuracaoRepository.findAll().get(0);
        return ResponseEntity.ok(new DadosConfiguracaoDTO(config));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_MASTER', 'ROLE_DONO', 'ROLE_GERENTE')")
    public ResponseEntity<DadosConfiguracaoDTO> atualizarConfiguracao(@RequestBody DadosConfiguracaoDTO dados) {
        Configuracao config = configuracaoRepository.findAll().get(0);

        if (dados.getInicioExpediente() != null) config.setInicioExpediente(dados.getInicioExpediente());
        if (dados.getFimExpediente() != null) config.setFimExpediente(dados.getFimExpediente());
        if (dados.getDiasFuncionamento() != null) {
            config.setDiasFuncionamento(dados.getDiasFuncionamento());
        }

        configuracaoRepository.save(config);
        return ResponseEntity.ok(new DadosConfiguracaoDTO(config));
    }
}