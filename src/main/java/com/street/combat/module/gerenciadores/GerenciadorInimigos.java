package com.street.combat.module.gerenciadores;

import com.street.combat.module.mapa.Mapa;
import com.street.combat.module.salas.Sala;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class GerenciadorInimigos {

    private  final GerenciadorSalas gerenciadorSalas;

    public GerenciadorInimigos(GerenciadorSalas gerenciadorSalas) {
        this.gerenciadorSalas = gerenciadorSalas;
    }

    public void levouDano(WebSocketSession session, String idInimigo) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            int danoDoJogador = sala.danoDoJogador(session.getId());
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                mapa.inimigoLevouDano(idInimigo, danoDoJogador);
                sala.atualizarInimigos();
            }
        }
    }

    public void itemLevouDano(WebSocketSession session, String idItem) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            int danoDoJogador = sala.danoDoJogador(session.getId());
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.itemLevouDano(session.getId(), idItem, danoDoJogador);
                sala.atualizarItens();
            }
        }
    }

    public void barreiraLevouDano(WebSocketSession session, String idBarreira) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            int danoDoJogador = sala.danoDoJogador(session.getId());
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.barreiraLevouDano(idBarreira, danoDoJogador);
                sala.atualizarBarreiras();
            }
        }
    }
}
