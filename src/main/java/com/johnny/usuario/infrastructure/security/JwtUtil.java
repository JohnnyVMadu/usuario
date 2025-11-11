package com.johnny.usuario.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // tem que ser grande pro HS512
    private static final String SECRET_KEY =
            "chaveSuperSecretaParaAssinaturaJWT123456789012345678901234567890";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1h

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // gera token com o e-mail no subject
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // <<< ESTE NOME AQUI É O QUE O TEU SERVICE ESTÁ CHAMANDO
    public String extrairEmailToken(String token) {
        return getClaims(token).getSubject();
    }

    // o filtro chama assim
    public String extractUsername(String token) {
        return extrairEmailToken(token);
    }

    // valida só se o token é bom
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // valida se o token é bom E se o e-mail dentro dele é o mesmo que o do usuário
    public boolean validarToken(String token, String username) {
        String emailDoToken = extrairEmailToken(token);
        return emailDoToken.equals(username) && validarToken(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
