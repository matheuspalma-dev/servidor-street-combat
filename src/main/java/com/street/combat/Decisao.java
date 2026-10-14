package com.street.combat;

import com.street.combat.module.gerenciadores.GerenciadorInimigos;
import com.street.combat.module.gerenciadores.GerenciadorJogador;
import com.street.combat.module.gerenciadores.GerenciadorSalas;
import com.street.combat.module.jogador.AtaqueRamona;
import com.street.combat.module.jogador.AtaqueScott;
import com.street.combat.module.salas.Sala;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        respostaServidor.put("salas", getSalasOnlineValidas(gerenciadorSalas.getSalasOnline().values().stream().toList()));
        session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketSession sessionSegura = sessions.get(session.getId());

        if (sessionSegura == null) return;

        Map<String, Object> mensagemRecebida = tradutor.readValue(message.getPayload(), Map.class);
        String tipo = (String) mensagemRecebida.get("tipo");

        if (tipo == null) {
            System.out.println("Tipo de mensagem não especificado.");
            return;
        }

        switch (tipo) {
            case "criarSala":
                String mapa = (String) mensagemRecebida.get("mapa");
                String personagem = (String) mensagemRecebida.get("personagem");
                String nomeCriador = (String) mensagemRecebida.get("nomeCriador");
                String skin = (String) mensagemRecebida.get("skin");
                gerenciadorSalas.criarSala(mapa, sessionSegura, personagem, nomeCriador, skin);
                atualizarSalas();
                break;
            case "entrarSala":
                String idSala = (String) mensagemRecebida.get("idSala");
                String personagemEntrar = (String) mensagemRecebida.get("personagem");
                String nomeJogador = (String) mensagemRecebida.get("nomeJogador");
                String skinEntrar = (String) mensagemRecebida.get("skin");
                gerenciadorSalas.entrarSala(idSala, sessionSegura, personagemEntrar, nomeJogador, skinEntrar);
                atualizarSalas();
                break;
            case "sairDaSala":
                gerenciadorSalas.sairSala(sessionSegura);
                atualizarSalas();
                break;
            case "receber_salas":
                Map<String, Object> respostaServidor = new HashMap<>();
                respostaServidor.put("tipo", "salasOnline");
                respostaServidor.put("salas", getSalasOnlineValidas(gerenciadorSalas.getSalasOnline().values().stream().toList()));
                sessionSegura.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
                break;
            case "andar":
                int x = ((Number) mensagemRecebida.get("x")).intValue();
                int y = ((Number) mensagemRecebida.get("y")).intValue();
                boolean direcao = (boolean) mensagemRecebida.get("direcao");
                gerenciadorJogador.jogadorAndou(sessionSegura, x, y, direcao);
                break;
            case "tentarAtacar":
                String skinJogador = (String) mensagemRecebida.get("skin");
                Object golpe;
                try {
                    if (skinJogador.equals("skin_rosa") || skinJogador.equals("skin_azul") || skinJogador.equals("skin_castanho") || skinJogador.equals("chapolin")){
                        golpe = AtaqueRamona.valueOf((String) mensagemRecebida.get("golpe"));
                    } else { // masculino
                        golpe = AtaqueScott.valueOf((String) mensagemRecebida.get("golpe"));
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Golpe inválido: " + mensagemRecebida.get("golpe"));
                    golpe = null;
                }

                if (golpe != null) {
                    gerenciadorJogador.tentativaGolpe(sessionSegura, golpe);
                }
                break;
            case "golpearInimigo":
                String idInimigo = (String) mensagemRecebida.get("idInimigo");
                boolean masculino = (boolean) mensagemRecebida.get("masculino");
                int danoDado = (int) mensagemRecebida.get("dano");
                Object golpeUsado;

                if (masculino){
                    golpeUsado = AtaqueScott.valueOf((String) mensagemRecebida.get("golpe"));
                } else {
                    golpeUsado = AtaqueRamona.valueOf((String) mensagemRecebida.get("golpe"));
                }
                gerenciadorInimigos.levouDano(sessionSegura, idInimigo, golpeUsado, danoDado);
                break;
            case "levarDano":
                int dano = ((Number) mensagemRecebida.get("dano")).intValue();
                gerenciadorJogador.recebeuDano(sessionSegura, dano);
                break;
            case "golpearItem":
                String idItem = (String) mensagemRecebida.get("idItem");
                int danoItem = (int) mensagemRecebida.get("dano");
                gerenciadorInimigos.itemLevouDano(danoItem, idItem);
                break;
            case "golpearBarreira":
                String idBarreira = (String) mensagemRecebida.get("idBarreira");
                int danoBarreira = (int) mensagemRecebida.get("dano");
                gerenciadorInimigos.barreiraLevouDano(sessionSegura, idBarreira, danoBarreira);
                break;
            case "recuperarVida":
                int vidaRecuperada = (int) mensagemRecebida.get("vidaRecuperada");
                gerenciadorJogador.recuperarVida(sessionSegura, vidaRecuperada);
                break;
            case "lutarComBoss":
                gerenciadorSalas.bossFight(sessionSegura);
                break;
            case "pegouItem":
                String idItemPegou = (String) mensagemRecebida.get("idItem");
                gerenciadorInimigos.pegouItem(sessionSegura, idItemPegou);
                break;
            case "danoNoBoss":
                String idBoss = (String) mensagemRecebida.get("idBoss");
                int danoBoss = (int) mensagemRecebida.get("dano");
                gerenciadorInimigos.danoNoBoss(sessionSegura, idBoss, danoBoss);
                break;
            case "comecarLuta":
                gerenciadorSalas.getIdSalaDoJogador(sessionSegura).iniciarBatalhaBoss();
                break;
            case "bossSeDefendeu":
                String idInimigoBoss = (String) mensagemRecebida.get("idInimigo");
                gerenciadorInimigos.bossSeDefender(sessionSegura, idInimigoBoss);
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
        respostaServidor.put("salas", getSalasOnlineValidas(gerenciadorSalas.getSalasOnline().values().stream().toList()));
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    private Set<String> getSalasOnlineValidas(List<Sala> salasOnline) {
        return salasOnline.stream()
                .filter(sala -> sala.getJogadores().size() < 2)
                .map(Sala::getId)
                .collect(Collectors.toSet());
    }
}
