package com.street.combat.module.gerenciadores;

import com.street.combat.module.salas.Sala;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class GerenciadorJogador {

    private final GerenciadorSalas gerenciadorSalas;

    public GerenciadorJogador(GerenciadorSalas gerenciadorSalas) {
        this.gerenciadorSalas = gerenciadorSalas;
    }

    public void jogadorAndou(WebSocketSession session, int x, int y, String direcao) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            sala.jogadorAndou(session.getId(), x, y, direcao);
        }
    }

    public void recebeuDano(WebSocketSession session, int dano) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            sala.jogadorLevouDano(session.getId(), dano);
        }
    }

    public void tentativaGolpe(WebSocketSession session) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            sala.jogadorTentouGolpe(session.getId());
        }
    }
}
