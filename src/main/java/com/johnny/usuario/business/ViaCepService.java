package com.johnny.usuario.business;

import com.johnny.usuario.infrastructure.clients.ViaCepClient;
import com.johnny.usuario.infrastructure.clients.ViaCepDTO;
import com.johnny.usuario.infrastructure.exceptions.ViaCepException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final ViaCepClient client;

    public ViaCepDTO buscarDadosEndereco(String cep) {

        String cepFormatado = processarCep(cep);

        try {
            return client.buscaDadosEndereco(cepFormatado);
        } catch (Exception e) {
            throw new ViaCepException("Erro ao buscar dados do CEP na API ViaCep", e);
        }
    }

    private String processarCep(String cep) {

        if (cep == null) {
            throw new ViaCepException("CEP não pode ser nulo");
        }

        // regra solicitada: CEP maior que 9 é inválido
        if (cep.length() > 9) {
            throw new ViaCepException("CEP informado possui mais de 9 caracteres");
        }

        String cepFormatado = cep
                .replace(" ", "")
                .replace("-", "");

        if (!cepFormatado.matches("\\d{8}")) {
            throw new ViaCepException("O CEP informado é inválido");
        }

        return cepFormatado;
    }
}
