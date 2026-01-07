package com.johnny.usuario.controller;

import com.johnny.usuario.business.UsuarioService;
import com.johnny.usuario.business.ViaCepService;
import com.johnny.usuario.business.dto.EnderecoDTO;
import com.johnny.usuario.business.dto.TelefoneDTO;
import com.johnny.usuario.business.dto.UsuarioDTO;
import com.johnny.usuario.infrastructure.clients.ViaCepDTO;
import com.johnny.usuario.infrastructure.security.JwtUtil;
import com.johnny.usuario.infrastructure.security.SecurityConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas agendadas")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ViaCepService viaCepService;

    // ---------------------------
    // LOGIN
    // ---------------------------
    @PostMapping("/login")
    public String login(@RequestBody UsuarioDTO usuarioDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuarioDTO.getEmail(), usuarioDTO.getSenha())
        );
        return "Bearer " + jwtUtil.gerarToken(authentication.getName());
    }


    // ---------------------------
    // POST - Criar usuário
    // ---------------------------
    @PostMapping
    public ResponseEntity<UsuarioDTO> salvarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.salvaUsuario(usuarioDTO));
    }


    // ---------------------------
    // GET - Buscar por email (livre)
    // ---------------------------
    @GetMapping
    public ResponseEntity<UsuarioDTO> buscaUsuarioPorEmailQuery(@RequestParam("email") String email) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorEmail(email));
    }


    // ---------------------------
    // GET /me (usuário autenticado)
    // ---------------------------
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> buscarProprio(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.buscarProprioUsuario(token));
    }


    // ---------------------------
    // PUT - Atualizar nome/email
    // ---------------------------
    @PutMapping
    public ResponseEntity<UsuarioDTO> atualizarDados(@RequestHeader("Authorization") String token,
                                                     @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarDadosBasicos(token, dto));
    }


    // ---------------------------
    // PATCH - Atualizar senha
    // ---------------------------
    @PatchMapping("/senha")
    public ResponseEntity<Void> atualizarSenha(@RequestHeader("Authorization") String token,
                                               @RequestBody UsuarioDTO dto) {
        usuarioService.atualizarSenha(token, dto.getSenha());
        return ResponseEntity.ok().build();
    }


    // ---------------------------
    // DELETE - Apagar a própria conta
    // ---------------------------
    @DeleteMapping
    public ResponseEntity<Void> deletarConta(@RequestHeader("Authorization") String token) {
        usuarioService.deletarProprioUsuario(token);
        return ResponseEntity.noContent().build();
    }


    // ---------------------------
    // ENDEREÇO E TELEFONE
    // (não mexemos)
    // ---------------------------
    @PutMapping("/endereco")
    public ResponseEntity<EnderecoDTO> atualizaEndereco(@RequestBody EnderecoDTO dto,
                                                        @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizaEndereco(id, dto));
    }

    @PutMapping("/telefone")
    public ResponseEntity<TelefoneDTO> atualizaTelefone(@RequestBody TelefoneDTO dto,
                                                        @RequestParam("id") Long id) {
        return ResponseEntity.ok(usuarioService.atualizaTelefone(id, dto));
    }

    @PostMapping("/endereco")
    public ResponseEntity<EnderecoDTO> cadastraEndereco(@RequestBody EnderecoDTO dto,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.cadastraEndereco(token, dto));
    }

    @PostMapping("/telefone")
    public ResponseEntity<TelefoneDTO> cadastraTelefone(@RequestBody TelefoneDTO dto,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioService.cadastraTelefone(token, dto));
    }

    @GetMapping("/endereco/{cep}")
    public ResponseEntity<ViaCepDTO> buscarDadosCep (@PathVariable("cep") String cep){
        return ResponseEntity.ok(viaCepService.buscarDadosEndereco(cep));
    }
}
