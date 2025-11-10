package com.johnny.usuario.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY =
            "chaveSuperSecretaParaAssinaturaJWT123456789012345678901234567890";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    // 🔹 Gera o token JWT
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 🔹 Extrai o e-mail (subject)
    public String extrairEmailToken(String token) {
        return getClaims(token).getSubject();
    }

    // 🔹 Alias em inglês para compatibilidade com JwtRequestFilter
    public String extractUsername(String token) {
        return extrairEmailToken(token);
    }

    // 🔹 Validação simples de token (sem comparar usuário)
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token não suportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token malformado: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Assinatura inválida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Token vazio ou nulo: " + e.getMessage());
        }
        return false;
    }

    // 🔹 Versão completa (compatível com o JwtRequestFilter)
    public boolean validarToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && validarToken(token);
    }

    // 🔹 Retorna os dados internos (claims)
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // usa SecretKey ✅
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 🔹 Gera a SecretKey correta
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
