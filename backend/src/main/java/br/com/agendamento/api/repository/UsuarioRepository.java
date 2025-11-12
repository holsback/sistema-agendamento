package br.com.agendamento.api.repository;

import br.com.agendamento.api.model.Usuario;
import br.com.agendamento.api.model.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<UserDetails> findByEmail(String email);

    List<Usuario> findByPerfil(PerfilUsuario perfil);

}