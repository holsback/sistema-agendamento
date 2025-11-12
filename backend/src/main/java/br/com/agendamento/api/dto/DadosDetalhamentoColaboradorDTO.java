package br.com.agendamento.api.dto;

import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import lombok.Getter;
import java.util.UUID;

/**
 * DTO "limpo" de Saída (Resposta) para o AdminController.
 * Esconde a senha e outros dados internos.
 */
@Getter
public class DadosDetalhamentoColaboradorDTO {

    private UUID id;
    private String nome;
    private String email;
    private String telefone;
    private PerfilUsuario perfil;

    /**
     * Construtor de Conversão (Tradução)
     * Converte a entidade (suja) 'Usuario' neste DTO (limpo).
     */
    public DadosDetalhamentoColaboradorDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.perfil = usuario.getPerfil();
    }
}