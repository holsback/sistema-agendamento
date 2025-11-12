package br.com.agendamento.api.controller;

import br.com.agendamento.api.dto.DadosLoginDTO;
import br.com.agendamento.api.dto.DadosRegistroClienteDTO;
import br.com.agendamento.api.dto.DadosTokenJWT;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import br.com.agendamento.api.repository.UsuarioRepository;
import br.com.agendamento.api.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Define as regras (5 tentativas, 5 minutos de bloqueio)
    private static final int MAX_TENTATIVAS = 5;
    private static final int TEMPO_BLOQUEIO_MINUTOS = 5;

    // "Memória" para contar as falhas de cada email
    private final Map<String, Integer> tentativasFalhas = new ConcurrentHashMap<>();

    // "Memória" para guardar quem está bloqueado e a que horas foi bloqueado
    private final Map<String, LocalDateTime> usuariosBloqueados = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosLoginDTO dados) {

        String email = dados.getEmail();

        if (usuariosBloqueados.containsKey(email)) {
            LocalDateTime horaDoBloqueio = usuariosBloqueados.get(email);
            LocalDateTime horaDeDesbloqueio = horaDoBloqueio.plusMinutes(TEMPO_BLOQUEIO_MINUTOS);

            // Se a hora atual AINDA É ANTES da hora de desbloqueio, barra o login.
            if (LocalDateTime.now().isBefore(horaDeDesbloqueio)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // Retorna Erro 429
                        .body("Conta bloqueada devido a muitas tentativas falhas. Tente novamente em " + TEMPO_BLOQUEIO_MINUTOS + " minutos.");
            } else {
                // Se o tempo já passou, desbloqueia o usuário
                usuariosBloqueados.remove(email);
                tentativasFalhas.remove(email);
            }
        }

        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(dados.getEmail(), dados.getSenha());

            // Tenta autenticar
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // --- Se chegou aqui, O LOGIN DEU CERTO ---
            tentativasFalhas.remove(email); // Limpa o contador de falhas

            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            String tokenJWT = tokenService.gerarToken(usuarioLogado);
            return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));

        } catch (BadCredentialsException e) {
            // --- Se chegou aqui, O LOGIN FALHOU (Senha errada) ---

            // 1. Incrementa o contador de falhas
            int tentativas = tentativasFalhas.getOrDefault(email, 0) + 1;
            tentativasFalhas.put(email, tentativas);

            // 2. Verifica se atingiu o limite
            if (tentativas >= MAX_TENTATIVAS) {
                // Bloqueia o usuário
                usuariosBloqueados.put(email, LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // Retorna 429
                        .body("Conta bloqueada devido a muitas tentativas falhas. Tente novamente em " + TEMPO_BLOQUEIO_MINUTOS + " minutos.");
            }

            // Se ainda não atingiu o limite, retorna o erro padrão
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity registrarCliente(@RequestBody @Valid DadosRegistroClienteDTO dados) {
        // (Lógica de registro)
        if (usuarioRepository.findByEmail(dados.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: Email já cadastrado.");
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dados.getNome());
        novoUsuario.setEmail(dados.getEmail());
        novoUsuario.setTelefone(dados.getTelefone());
        novoUsuario.setSenha(passwordEncoder.encode(dados.getSenha()));
        novoUsuario.setPerfil(PerfilUsuario.ROLE_CLIENTE);
        usuarioRepository.save(novoUsuario);
        return ResponseEntity.ok("Usuário cliente registrado com sucesso!");
    }
}