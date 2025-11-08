package com.johnny.usuario.business;

import com.johnny.usuario.business.converter.UsuarioConverter;
import com.johnny.usuario.business.dto.UsuarioDTO;
import com.johnny.usuario.infrastructure.entity.Usuario;
import com.johnny.usuario.infrastructure.exceptions.ConflictException;
import com.johnny.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.johnny.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        boolean existe = usuarioRepository.existsByEmail(email);
        if (existe) {
            throw new ConflictException("Email já cadastrado " + email);
        }
    }

    public boolean emailExisteRetorno(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email não encontrado " + email));
    }
    public void deletaUsuarioPorEmail(@PathVariable String email){
        usuarioRepository.deleteByEmail(email);
    }
}
