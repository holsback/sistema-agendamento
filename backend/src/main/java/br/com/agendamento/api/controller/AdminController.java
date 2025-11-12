package br.com.agendamento.api.controller;

import br.com.agendamento.api.dto.DadosAtualizacaoColaboradorDTO;
import br.com.agendamento.api.dto.DadosDetalhamentoColaboradorDTO;
import br.com.agendamento.api.dto.DadosRegistroColaboradorDTO;
import br.com.agendamento.api.exception.ResourceNotFoundException;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import br.com.agendamento.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Cria um novo Colaborador (Dono, Gerente, Profissional)
     */
    @PostMapping("/criar-colaborador")
    @PreAuthorize(
            "(hasRole('ROLE_MASTER') and #dados.perfil.name() == 'ROLE_DONO') or " +
                    "(hasRole('ROLE_DONO') and (#dados.perfil.name() == 'ROLE_GERENTE' or #dados.perfil.name() == 'ROLE_PROFISSIONAL')) or " +
                    "(hasRole('ROLE_GERENTE') and #dados.perfil.name() == 'ROLE_PROFISSIONAL')"
    )
    public ResponseEntity criarColaborador(@RequestBody @Valid DadosRegistroColaboradorDTO dados) {
        if (usuarioRepository.findByEmail(dados.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: Email já cadastrado.");
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dados.getNome());
        novoUsuario.setEmail(dados.getEmail());
        novoUsuario.setTelefone(dados.getTelefone());
        novoUsuario.setSenha(passwordEncoder.encode(dados.getSenha()));
        novoUsuario.setPerfil(dados.getPerfil());
        usuarioRepository.save(novoUsuario);
        return ResponseEntity.ok("Colaborador (" + dados.getPerfil().name() + ") registrado com sucesso!");
    }

    /**
     * Deleta um Colaborador.
     */
    @DeleteMapping("/deletar-colaborador/{id}")
    @PreAuthorize(
            "@usuarioRepository.findById(#id).isPresent() and " +
                    "authentication.name != @usuarioRepository.findById(#id).get().getEmail() and (" +
                    "(hasRole('ROLE_MASTER') and @usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_DONO') or " +
                    "(hasRole('ROLE_DONO') and (@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_GERENTE' or @usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_PROFISSIONAL')) or " +
                    "(hasRole('ROLE_GERENTE') and @usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_PROFISSIONAL')" +
                    ")"
    )
    public ResponseEntity deletarColaborador(@PathVariable UUID id) {
        Usuario usuarioParaDeletar = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado."));
        try {
            usuarioRepository.delete(usuarioParaDeletar);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Erro: Não é possível deletar este usuário pois ele está ligado a outros registros (ex: agendamentos).");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista Colaboradores (filtrado por hierarquia)
     */
    @GetMapping("/listar-colaboradores")
    @PreAuthorize("hasAnyRole('ROLE_MASTER', 'ROLE_DONO', 'ROLE_GERENTE')")
    public ResponseEntity<List<DadosDetalhamentoColaboradorDTO>> listarColaboradores(Authentication authentication) {
        PerfilUsuario perfilLogado = ((Usuario) authentication.getPrincipal()).getPerfil();
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        List<DadosDetalhamentoColaboradorDTO> listaFiltrada;

        if (perfilLogado == PerfilUsuario.ROLE_MASTER) {
            listaFiltrada = todosUsuarios.stream()
                    .filter(u -> u.getPerfil() == PerfilUsuario.ROLE_DONO ||
                            u.getPerfil() == PerfilUsuario.ROLE_GERENTE ||
                            u.getPerfil() == PerfilUsuario.ROLE_PROFISSIONAL)
                    .map(DadosDetalhamentoColaboradorDTO::new)
                    .collect(Collectors.toList());
        } else if (perfilLogado == PerfilUsuario.ROLE_DONO) {
            listaFiltrada = todosUsuarios.stream()
                    .filter(u -> u.getPerfil() == PerfilUsuario.ROLE_GERENTE ||
                            u.getPerfil() == PerfilUsuario.ROLE_PROFISSIONAL)
                    .map(DadosDetalhamentoColaboradorDTO::new)
                    .collect(Collectors.toList());
        } else {
            listaFiltrada = todosUsuarios.stream()
                    .filter(u -> u.getPerfil() == PerfilUsuario.ROLE_PROFISSIONAL)
                    .map(DadosDetalhamentoColaboradorDTO::new)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(listaFiltrada);
    }

    /**
     * Atualiza um Colaborador (Nome, Telefone, Senha, Perfil)
     */
    @PutMapping("/atualizar-colaborador/{id}")
    @PreAuthorize(
            "@usuarioRepository.findById(#id).isPresent() and " +
                    "authentication.name != @usuarioRepository.findById(#id).get().getEmail() and " +
                    "((" +
                    "(hasRole('ROLE_MASTER') and (" +
                    "@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_DONO' or " +
                    "@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_GERENTE' or " +
                    "@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_PROFISSIONAL'" +
                    ")) or " +
                    "(hasRole('ROLE_DONO') and (" +
                    "@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_GERENTE' or " +
                    "@usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_PROFISSIONAL'" +
                    ")) or " +
                    "(hasRole('ROLE_GERENTE') and @usuarioRepository.findById(#id).get().getPerfil().name() == 'ROLE_PROFISSIONAL')" +
                    "))" +
                    "and" +
                    "(" +
                    "#dados.perfil == null or " +
                    "(" +
                    "(hasRole('ROLE_MASTER') and (#dados.perfil.name() == 'ROLE_DONO' or #dados.perfil.name() == 'ROLE_GERENTE' or #dados.perfil.name() == 'ROLE_PROFISSIONAL')) or " +
                    "(hasRole('ROLE_DONO') and (#dados.perfil.name() == 'ROLE_GERENTE' or #dados.perfil.name() == 'ROLE_PROFISSIONAL')) or " +
                    "(hasRole('ROLE_GERENTE') and #dados.perfil.name() == 'ROLE_PROFISSIONAL')" +
                    ")" +
                    ")"
    )
    public ResponseEntity<DadosDetalhamentoColaboradorDTO> atualizarColaborador(
            @PathVariable UUID id,
            @RequestBody @Valid DadosAtualizacaoColaboradorDTO dados
    ) {
        Usuario colaborador = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador com ID " + id + " não encontrado."));

        if (dados.getNome() != null && !dados.getNome().isBlank()) {
            colaborador.setNome(dados.getNome());
        }
        if (dados.getTelefone() != null && !dados.getTelefone().isBlank()) {
            colaborador.setTelefone(dados.getTelefone());
        }
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            colaborador.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        if (dados.getPerfil() != null) {
            colaborador.setPerfil(dados.getPerfil());
        }

        Usuario usuarioSalvo = usuarioRepository.save(colaborador);
        return ResponseEntity.ok(new DadosDetalhamentoColaboradorDTO(usuarioSalvo));
    }
}