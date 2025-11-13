package com.johnny.usuario.business;

import com.johnny.usuario.business.converter.UsuarioConverter;
import com.johnny.usuario.business.dto.EnderecoDTO;
import com.johnny.usuario.business.dto.TelefoneDTO;
import com.johnny.usuario.business.dto.UsuarioDTO;
import com.johnny.usuario.infrastructure.entity.Endereco;
import com.johnny.usuario.infrastructure.entity.Telefone;
import com.johnny.usuario.infrastructure.entity.Usuario;
import com.johnny.usuario.infrastructure.exceptions.ConflictException;
import com.johnny.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.johnny.usuario.infrastructure.repository.EnderecoRepository;
import com.johnny.usuario.infrastructure.repository.TelefoneRepository;
import com.johnny.usuario.infrastructure.repository.UsuarioRepository;
import com.johnny.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;


    // ---------------------------
    // POST - Criar usuário
    // ---------------------------
    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Email já cadastrado " + email);
        }
    }


    // ---------------------------
    // GET - Buscar por e-mail (livre)
    // ---------------------------
    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email não encontrado " + email));
        return usuarioConverter.paraUsuarioDTO(usuario);
    }


    // ---------------------------
    // GET - Buscar o próprio usuário (/me)
    // ---------------------------
    public UsuarioDTO buscarProprioUsuario(String token) {
        String email = extrairEmail(token);
        return buscarUsuarioPorEmail(email);
    }


    // ---------------------------
    // PUT - Atualizar nome + email
    // ---------------------------
    public UsuarioDTO atualizarDadosBasicos(String token, UsuarioDTO dto) {
        String emailToken = extrairEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(emailToken)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            emailExiste(dto.getEmail()); // valida se novo email já existe
            usuario.setEmail(dto.getEmail());
        }

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }

        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }


    // ---------------------------
    // PATCH - Atualizar senha
    // ---------------------------
    public void atualizarSenha(String token, String novaSenha) {
        String email = extrairEmail(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }


    // ---------------------------
    // DELETE - Apagar o próprio usuário
    // ---------------------------
    public void deletarProprioUsuario(String token) {
        String email = extrairEmail(token);
        usuarioRepository.deleteByEmail(email);
    }


    // ---------------------------
    // Métodos auxiliares
    // ---------------------------
    private String extrairEmail(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extrairEmailToken(token);
    }


    // ---------------------------
    // ENDEREÇO E TELEFONE
    // (não mexemos)
    // ---------------------------
    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {
        Endereco entity = enderecoRepository.findById(idEndereco)
                .orElseThrow(() -> new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto) {
        Telefone entity = telefoneRepository.findById(idTelefone)
                .orElseThrow(() -> new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        String email = extrairEmail(token);
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado " + email));

        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto, usuario.getId());
        Endereco enderecoEntity = enderecoRepository.save(endereco);
        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        String email = extrairEmail(token);
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado " + email));

        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

}
