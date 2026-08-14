package com.street.combat.module.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private boolean banido;
}
