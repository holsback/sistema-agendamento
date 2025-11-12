package br.com.agendamento.api;

import br.com.agendamento.api.model.Servico;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import br.com.agendamento.api.repository.ServicoRepository;
import br.com.agendamento.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import br.com.agendamento.api.model.Configuracao;
import br.com.agendamento.api.repository.ConfiguracaoRepository;
import java.time.DayOfWeek;
import br.com.agendamento.api.model.Agendamento;
import br.com.agendamento.api.repository.AgendamentoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalTime;
import java.util.Set;
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    @Override
    public void run(String... args) throws Exception {

        // --- PLANTANDO USUÁRIOS ---
        criarUsuarioSeNaoExistir("Cliente Teste", "cliente@email.com", "(11) 91111-1111", "senha123", PerfilUsuario.ROLE_CLIENTE);
        criarUsuarioSeNaoExistir("Maria Profissional", "maria@profissional.com", "(11) 92222-2222", "senha123", PerfilUsuario.ROLE_PROFISSIONAL);
        criarUsuarioSeNaoExistir("João Barbeiro", "joao@profissional.com", "(11) 97777-7777", "senha123", PerfilUsuario.ROLE_PROFISSIONAL);
        criarUsuarioSeNaoExistir("Geraldo Gerente", "gerente@email.com", "(11) 93333-3333", "senha123", PerfilUsuario.ROLE_GERENTE);
        criarUsuarioSeNaoExistir("Douglas Dono", "dono@email.com", "(11) 94444-4444", "senha123", PerfilUsuario.ROLE_DONO);
        criarUsuarioSeNaoExistir("Bruno Master", "master@email.com", "(11) 95555-5555", "senha123", PerfilUsuario.ROLE_MASTER);

        // --- PLANTANDO SERVIÇOS ---
        criarServicoSeNaoExistir("Corte Masculino", "Corte tradicional com máquina e tesoura", new BigDecimal("50.00"), 30);
        criarServicoSeNaoExistir("Corte Feminino", "Corte, lavagem e secagem", new BigDecimal("120.00"), 60);
        criarServicoSeNaoExistir("Barba", "Barba desenhada com toalha quente", new BigDecimal("40.00"), 30);
        criarServicoSeNaoExistir("Pintura Completa", "Tintura e hidratação", new BigDecimal("250.00"), 120);
        criarConfiguracaoPadraoSeNaoExistir();
        criarAgendamentosIniciais();
    }



    private void criarUsuarioSeNaoExistir(String nome, String email, String telefone, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setTelefone(telefone);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPerfil(perfil);
            usuarioRepository.save(usuario);
            System.out.println(">>> Usuário " + perfil.name() + " criado: " + email);
        }
    }

    private void criarServicoSeNaoExistir(String nome, String descricao, BigDecimal preco, Integer duracao) {
        boolean existe = servicoRepository.findAll().stream().anyMatch(s -> s.getNome().equals(nome));

        if (!existe) {
            Servico servico = new Servico();
            servico.setNome(nome);
            servico.setDescricao(descricao);
            servico.setPreco(preco);
            servico.setDuracaoMinutos(duracao);
            servicoRepository.save(servico);
            System.out.println(">>> Serviço criado: " + nome + " (" + duracao + " min)");
        }
    }

    private void criarAgendamentosIniciais() {
        // Se já tiver agendamentos, não faz nada (para não duplicar)
        if (agendamentoRepository.count() > 0) {
            return;
        }

        System.out.println(">>> Plantando agendamentos iniciais...");

        try {
            // Pega os usuários que vamos usar
            Usuario maria = (Usuario) usuarioRepository.findByEmail("maria@profissional.com").get();
            Usuario joao = (Usuario) usuarioRepository.findByEmail("joao@profissional.com").get();
            Usuario cliente = (Usuario) usuarioRepository.findByEmail("cliente@email.com").get();

            // Pega os serviços
            Servico corteM = servicoRepository.findAll().stream().filter(s -> s.getNome().equals("Corte Masculino")).findFirst().get();
            Servico barba = servicoRepository.findAll().stream().filter(s -> s.getNome().equals("Barba")).findFirst().get();
            Servico corteF = servicoRepository.findAll().stream().filter(s -> s.getNome().equals("Corte Feminino")).findFirst().get();

            // Agendamento 1: Maria (Amanhã às 10h)
            LocalDateTime data1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);
            Agendamento a1 = new Agendamento();
            a1.setCliente(cliente);
            a1.setProfissional(maria);
            a1.setServicos(List.of(corteF));
            a1.setDataHora(data1);
            a1.setDataHoraFim(data1.plusMinutes(corteF.getDuracaoMinutos()));
            a1.setStatus("Pendente");
            agendamentoRepository.save(a1);

            // Agendamento 2: Maria (Amanhã às 14h - COMBO)
            LocalDateTime data2 = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0);
            int duracaoCombo = corteM.getDuracaoMinutos() + barba.getDuracaoMinutos();
            Agendamento a2 = new Agendamento();
            a2.setCliente(cliente);
            a2.setProfissional(maria);
            a2.setServicos(List.of(corteM, barba));
            a2.setDataHora(data2);
            a2.setDataHoraFim(data2.plusMinutes(duracaoCombo));
            a2.setStatus("Pendente");
            agendamentoRepository.save(a2);

            // Agendamento 3: João (Depois de amanhã às 11h)
            LocalDateTime data3 = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0).withSecond(0);
            Agendamento a3 = new Agendamento();
            a3.setCliente(cliente);
            a3.setProfissional(joao);
            a3.setServicos(List.of(corteM));
            a3.setDataHora(data3);
            a3.setDataHoraFim(data3.plusMinutes(corteM.getDuracaoMinutos()));
            a3.setStatus("Pendente");
            agendamentoRepository.save(a3);

            System.out.println(">>> 3 agendamentos de teste criados.");

        } catch (Exception e) {
            System.err.println("!!! Erro ao plantar agendamentos iniciais: " + e.getMessage());
            // (Isso pode acontecer se os usuários/serviços padrões não forem encontrados)
        }
    }

    private void criarConfiguracaoPadraoSeNaoExistir() {
        if (configuracaoRepository.count() == 0) {
            Configuracao config = new Configuracao();
            config.setInicioExpediente(LocalTime.of(8, 0));
            config.setFimExpediente(LocalTime.of(18, 0));
            config.setDiasFuncionamento(Set.of(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            ));
            configuracaoRepository.save(config);
            System.out.println(">>> Configuração padrão criada: Seg-Sáb, 08h às 18h");
        }
    }
}