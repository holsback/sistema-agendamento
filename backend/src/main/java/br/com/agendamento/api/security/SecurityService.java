package br.com.agendamento.api.security;

import br.com.agendamento.api.model.Agendamento;
import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Este serviço é usado pelas regras de segurança @PreAuthorize (SpEL)
 * para verificações de lógica complexa.
 */
@Service("securityService") // O nome "securityService" é o que o @PreAuthorize usa
public class SecurityService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    /**
     * Verifica se o usuário logado (authentication) é o "dono" (cliente)
     * do agendamento que ele está tentando acessar.
     *
     * @param authentication O objeto do usuário logado.
     * @param agendamentoId O ID (UUID) do agendamento que ele quer acessar.
     * @return true se ele for o dono, false caso contrário.
     */
    public boolean isAgendamentoOwner(Authentication authentication, UUID agendamentoId) {
        // Pega o email (username) do usuário logado
        String emailUsuarioLogado = authentication.getName();

        // Busca o agendamento no banco
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(agendamentoId);

        // Se o agendamento não existir, nega o acesso
        if (agendamentoOpt.isEmpty()) {
            return false;
        }

        // Pega o email do CLIENTE dono daquele agendamento
        String emailDonoAgendamento = agendamentoOpt.get().getCliente().getEmail();

        // Compara os dois emails
        return emailUsuarioLogado.equals(emailDonoAgendamento);
    }
}