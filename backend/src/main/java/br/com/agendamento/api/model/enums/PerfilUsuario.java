package br.com.agendamento.api.model.enums;

/**
 * Esta é a nossa lista fixa de Perfis de Usuário.
 * O Spring Security usa o prefixo "ROLE_"
 * por convenção para identificar as "Roles" (perfis).
 */
public enum PerfilUsuario {

    ROLE_MASTER,
    ROLE_DONO,
    ROLE_GERENTE,
    ROLE_PROFISSIONAL,
    ROLE_CLIENTE

}