package com.street.combat.module.usuario;

import com.street.combat.module.ranking.RankingService;
import com.street.combat.module.usuario.dto.UsuarioRequestDTO;
import com.street.combat.module.usuario.dto.UsuarioResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RankingService rankingService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RankingService rankingService
    )
    {
        this.usuarioRepository = usuarioRepository;
        this.rankingService = rankingService;
    }

    public UsuarioResponseDTO cadastro(UsuarioRequestDTO usuarioRequestDTO){
        Usuario usuario = criarUsuario(usuarioRequestDTO);
        if (usuario.getNome().contains("MD") || usuario.getNome().contains("md") || usuario.getNome().contains("mD") || usuario.getNome().contains("Md")) {
            usuario.setBanido(true);
        } else if (usuario.getNome().contains("67") || usuario.getNome().contains("42")){
            usuario.setBanido(true);
        }
        rankingService.adicionarNoRanking(usuario);
        usuarioRepository.save(usuario);
        return devolverUsuario(usuario);
    }

    public UsuarioResponseDTO login(UsuarioRequestDTO usuarioRequestDTO){
        Usuario usuario = procurarUsuario(usuarioRequestDTO);

        if (verificarSenha(usuario, usuarioRequestDTO)) {
            return devolverUsuario(usuario);
        } else {
            return null;
        }

    }

    private Usuario criarUsuario(UsuarioRequestDTO usuarioRequestDTO){
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioRequestDTO.getNome());
        usuario.setEmail(usuarioRequestDTO.getEmail());
        String senhaCriptografada = passwordEncoder.encode(usuarioRequestDTO.getSenha());
        usuario.setSenha(senhaCriptografada);
        usuario.setBanido(false);
        return usuario;
    }

    private UsuarioResponseDTO devolverUsuario(Usuario usuario){
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(usuario.getId());
        usuarioResponseDTO.setNome(usuario.getNome());
        usuarioResponseDTO.setBanido(usuario.isBanido());
        return usuarioResponseDTO;
    }

    private Usuario procurarUsuario(UsuarioRequestDTO usuarioRequestDTO){
        return usuarioRepository.findByNome(usuarioRequestDTO.getNome())
                .orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
    }

    private boolean verificarSenha(Usuario usuario, UsuarioRequestDTO usuarioRequestDTO){
        return passwordEncoder.matches(usuarioRequestDTO.getSenha(), usuario.getSenha());
    }
}
