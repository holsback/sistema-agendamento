package br.com.agendamento.api.security;

import br.com.agendamento.api.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
/**
 * Esta é a nossa "Fábrica de Crachás" (JWT).
 * @Service -> Avisa ao Spring: "Ei, esta é uma classe de 'serviço'.
 * Crie uma 'instância' dela para mim, pois outros (como o "Leão de Chácara")
 * vão precisar usá-la."
 */
@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String jwtSecret;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(jwtSecret);

            String token = JWT.create()
                    .withIssuer("API Agenda.Facil")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(gerarDataDeExpiracao())
                    .withClaim("role", usuario.getPerfil().name())
                    .sign(algoritmo);

            return token;

        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar o token JWT.", exception);
        }
    }

    public String validarTokenEObterEmail(String tokenJWT) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(jwtSecret);

            return JWT.require(algoritmo)
                    .withIssuer("API Agenda.Facil")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();

        } catch (JWTVerificationException exception){
            return null;
        }
    }

    private Instant gerarDataDeExpiracao() {
        return LocalDateTime.now()
                .plusHours(2)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}