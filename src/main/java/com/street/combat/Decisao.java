package com.street.combat;

import com.street.combat.module.gerenciadores.GerenciadorInimigos;
import com.street.combat.module.gerenciadores.GerenciadorJogador;
import com.street.combat.module.gerenciadores.GerenciadorSalas;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Decisao extends TextWebSocketHandler {

    private final GerenciadorSalas gerenciadorSalas;
    private final GerenciadorJogador gerenciadorJogador;
    private final GerenciadorInimigos gerenciadorInimigos;

    //jogadores que não estão em alguma sala
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper tradutor = new ObjectMapper();

    public Decisao(GerenciadorSalas gerenciadorSalas, GerenciadorJogador gerenciadorJogador,
                   GerenciadorInimigos gerenciadorInimigos) {
        this.gerenciadorSalas = gerenciadorSalas;
        this.gerenciadorJogador = gerenciadorJogador;
        this.gerenciadorInimigos = gerenciadorInimigos;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        WebSocketSession sessionSegura = new ConcurrentWebSocketSessionDecorator(session, 10000, 512 * 1024);
        sessions.put(session.getId(), sessionSegura);
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "salasOnline");
        respostaServidor.put("salas", gerenciadorSalas.getSalasOnline().keySet());
        respostaServidor.put("idSessao", sessionSegura.getId());
        session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketSession sessionSegura = sessions.get(session.getId());

        if (sessionSegura == null) return;

        Map<String, Object> mensagemRecebida = tradutor.readValue(message.getPayload(), Map.class);
        String tipo = (String) mensagemRecebida.get("tipo");

        switch (tipo) {
            case "enviarId":

            case "criarSala":
                String mapa = (String) mensagemRecebida.get("mapa");
                String personagem = (String) mensagemRecebida.get("personagem");
                String nomeCriador = (String) mensagemRecebida.get("nomeCriador");
                gerenciadorSalas.criarSala(mapa, sessionSegura, personagem, nomeCriador);
                atualizarSalas();
                break;
            case "entrarSala":
                String idSala = (String) mensagemRecebida.get("idSala");
                String personagemEntrar = (String) mensagemRecebida.get("personagem");
                gerenciadorSalas.entrarSala(idSala, sessionSegura, personagemEntrar);
                atualizarSalas();
                break;
            case "sairDaSala":
                gerenciadorSalas.sairSala(sessionSegura);
                atualizarSalas();
                break;
            case "receber_salas":
                Map<String, Object> respostaServidor = new HashMap<>();
                respostaServidor.put("tipo", "salasOnline");
                respostaServidor.put("salas", gerenciadorSalas.getSalasOnline().keySet());
                sessionSegura.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
                break;
            case "andar":
                int x = ((Number) mensagemRecebida.get("x")).intValue();
                int y = ((Number) mensagemRecebida.get("y")).intValue();
                String direcao = (String) mensagemRecebida.get("direcao");
                gerenciadorJogador.jogadorAndou(sessionSegura, x, y, direcao);
                break;
            case "golpearInimigo":
                String idInimigo = (String) mensagemRecebida.get("idInimigo");
                gerenciadorInimigos.levouDano(sessionSegura, idInimigo);
                break;
            case "levarDano":
                int dano = ((Number) mensagemRecebida.get("dano")).intValue();
                gerenciadorJogador.recebeuDano(sessionSegura, dano);
                break;
            case "golpearItem":
                String idItem = (String) mensagemRecebida.get("idItem");
                gerenciadorInimigos.itemLevouDano(sessionSegura, idItem);
                break;
            case "golpearBarreira":
                String idBarreira = (String) mensagemRecebida.get("idBarreira");
                gerenciadorInimigos.barreiraLevouDano(sessionSegura, idBarreira);
                break;
            default:
                System.out.println("deu ruim em algo ai");
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        WebSocketSession sessionSegura = sessions.remove(session.getId());
        if (sessionSegura != null){
            gerenciadorSalas.sairSala(sessionSegura);
        }
    }

    public void atualizarSalas() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarSalasOnline");
        respostaServidor.put("salas", gerenciadorSalas.getSalasOnline().keySet());
        for(WebSocketSession session : sessions.values()){            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }
}
