package com.street.combat.module.gerenciadores;

import com.street.combat.module.salas.Sala;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Service
public class GerenciadorSalas {

    private Map<String, Sala> salasOnline = new ConcurrentHashMap<>();
    // jogadores que estão em alguma sala, chave = id da sessão, valor = id da sala
    private Map<String, String> jogadoresParaSala = new ConcurrentHashMap<>();
    private final ObjectMapper tradutor = new ObjectMapper();

    public void criarSala(String mapa, WebSocketSession session, String personagem) throws IOException {
        Sala novaSala = new Sala(mapa);
        novaSala.adicionarJogador(session ,session.getId(), personagem);
        jogadoresParaSala.put(session.getId(), novaSala.getId());
        salasOnline.put(novaSala.getId(), novaSala);
    }

    public void entrarSala(String idSala, WebSocketSession session, String personagem) throws IOException {
        Sala sala = salasOnline.get(idSala);
        if (sala != null){
            sala.adicionarJogador(session, session.getId(), personagem);
            jogadoresParaSala.put(session.getId(), sala.getId());
        }
    }

    public void sairSala(WebSocketSession session) throws IOException {
        String idSala = jogadoresParaSala.get(session.getId());
        Sala sala = getIdSalaDoJogador(session);
        if (sala != null) {
            sala.removerDaSala(session.getId());
            if (sala.salaVazia()) {
                sala.getExecutor().shutdownNow();
                salasOnline.remove(idSala);
            }
        }
        jogadoresParaSala.remove(session.getId());

    }

    public Sala getIdSalaDoJogador(WebSocketSession session) {
        String idSala = jogadoresParaSala.get(session.getId());
        if (idSala != null) {
            Sala salaJogador = salasOnline.get(idSala);
            if (salaJogador != null){
                return salaJogador;
            }
        }
        return null;
    }
}
