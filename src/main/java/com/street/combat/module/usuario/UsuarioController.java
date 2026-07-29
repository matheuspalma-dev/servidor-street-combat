package com.street.combat.module.usuario;

import com.street.combat.module.usuario.dto.UsuarioRequestDTO;
import com.street.combat.module.usuario.dto.UsuarioResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDTO> cadastro(@RequestBody UsuarioRequestDTO usuarioDTO){
        UsuarioResponseDTO usuarioResponseDTO = usuarioService.cadastro(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponseDTO);
    }
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody UsuarioRequestDTO usuarioDTO){
        UsuarioResponseDTO usuarioResponseDTO = usuarioService.login(usuarioDTO);
        if (usuarioResponseDTO != null) {
            return ResponseEntity.ok(usuarioResponseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
