package com.street.combat.module.salas;

import com.street.combat.module.Inimigos.Inimigo;
import com.street.combat.module.jogador.AtaqueRamona;
import com.street.combat.module.jogador.AtaqueScott;
import com.street.combat.module.jogador.AtributosExtra;
import com.street.combat.module.jogador.Jogador;
import com.street.combat.module.mapa.Mapa;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Sala {

    private final String id;
    //depois eu removo
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Jogador> jogadores = new ConcurrentHashMap<>();
    private final Mapa mapa;
    private final ObjectMapper tradutor = new ObjectMapper();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();

    @SneakyThrows
    public Sala(String nomeMapa, String nomeCriador) {
        this.id = nomeCriador;
        this.mapa = new Mapa(nomeMapa);
        executor.scheduleAtFixedRate(() -> {
            try {
                inimigoEscolherJogadorParaAtacar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    // Funções relacionadas a salas
    public void adicionarJogador(WebSocketSession session, String id, String personagem, String nomeJogador) throws IOException {
        Jogador jogador = new Jogador(personagem, id, this.mapa.getNome(), nomeJogador);
        jogadores.put(id, jogador);
        sessions.put(id, session);
        atualizarJogadores();
        atualizarItens();
    }

    public void removerDaSala(String id) throws IOException {
        jogadores.remove(id);
        sessions.remove(id);
        atualizarJogadores();
    }

    public boolean salaVazia() {
        return jogadores.isEmpty();
    }

    // funções relacionadas a jogadores
    public void jogadorAndou(String id, int x, int y, String direcao) throws IOException {
        Jogador jogador = jogadores.get(id);
        jogador.mover(x, y, direcao);
        atualizarJogadores();
    }

    public void jogadorTentouGolpe(String id) throws IOException {
        Jogador jogador = jogadores.get(id);
        if (jogador != null){
            String golpe = escolherAtaque(jogador.getPersonagem());
            Map<String, Object> respostaServidor = new HashMap<>();
            respostaServidor.put("tipo", "jogadorTentouGolpe");
            respostaServidor.put("golpe", golpe);
            respostaServidor.put("idJogador", id);
            for(WebSocketSession session : sessions.values()){
                if (session.isOpen()){
                    session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
                }
            }
        }
    }

    private String escolherAtaque(String personagem){
        if (personagem.equals("feminimo")){
            AtaqueRamona[] ataques = AtaqueRamona.values();
            return ataques[random.nextInt(ataques.length)].name();
        } else if (personagem.equals("masculino")){
            AtaqueScott[] ataques = AtaqueScott.values();
            return ataques[random.nextInt(ataques.length)].name();
        }
        return null;
    }

    public void jogadorLevouDano(String id, int dano) throws IOException {
        Jogador jogador = jogadores.get(id);
        if (jogador != null) {
            jogador.levarDano(dano);
            if (!jogador.estaVivo()) {
                jogadores.remove(id);
                sessions.remove(id);
            }
            atualizarJogadores();
        }
    }

    public int danoDoJogador(String id) {
        Jogador jogador = jogadores.get(id);
        if (jogador != null) {
            if (jogador.getAtributosExtras().contains(AtributosExtra.DANO_EXTRA)){
                return jogador.getDano() + 2;
            } else {
                return jogador.getDano();
            }
        }
        return 0;
    }

    public void atualizarJogadores() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarJogadores");
        respostaServidor.put("jogadores", jogadores);
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    private void jogadorReceberAtributoExtra(Jogador jogador, String nomeItem){
        AtributosExtra atributoExtra;
        switch (nomeItem){
            case "recuperarVida":
                atributoExtra = AtributosExtra.RECUPERAR_VIDA;
                break;
            case "danoExtra":
                atributoExtra = AtributosExtra.DANO_EXTRA;
                break;
            case "escudoExtra":
                atributoExtra = AtributosExtra.DEFESA_EXTRA;
                break;
            case "veneno":
                atributoExtra = AtributosExtra.VENENO;
                break;
            case "velocidadeExtra":
                atributoExtra = AtributosExtra.VELOCIDADE_EXTRA;
                break;
            default:
                atributoExtra = null;
                break;
        }
        if (atributoExtra != null) {
            jogador.adicionarAtributoExtra(atributoExtra);
            jogadorReceberEfeitosDoItem(jogador, atributoExtra);
        }

    }

    private void jogadorReceberEfeitosDoItem(Jogador jogador, AtributosExtra atributoExtra){
        if (!jogador.estaVivo()){
            return;
        }

        ScheduledExecutorService efeitoExecutor = Executors.newSingleThreadScheduledExecutor();

        long tempoInicio = System.currentTimeMillis();
        long duracaoTotalMs = 60_000;

        efeitoExecutor.scheduleAtFixedRate(() -> {
            try {
                long tempoDecorrido = System.currentTimeMillis() - tempoInicio;

                if (tempoDecorrido >= duracaoTotalMs || !jogadores.containsKey(jogador.getId()) || !jogador.estaVivo()) {
                    jogador.removerAtributoExtra(atributoExtra);
                    atualizarJogadores();
                    efeitoExecutor.shutdown();
                    return;
                }

                switch (atributoExtra) {
                    case RECUPERAR_VIDA:
                        jogador.curar();
                        atualizarJogadores();
                        break;

                    case VENENO:
                        jogadorLevouDano(jogador.getId(), 1);
                        break;
                    //esperam o tempo passar, não precisam de ação a cada tick
                    case DANO_EXTRA:
                    case DEFESA_EXTRA:
                    case VELOCIDADE_EXTRA:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                efeitoExecutor.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    // Funções relacionadas a inimigos
    public void atualizarInimigos() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarInimigos");
        respostaServidor.put("inimigos", mapa.getInimigos());
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    private void inimigoEscolherJogadorParaAtacar() throws IOException {
        for (Inimigo inimigo : mapa.getInimigos().values()){
            Jogador alvo = null;
            int menorDistancia = 0;
            for (Jogador jogador : jogadores.values()){
                int distanciaX = Math.abs(inimigo.getX() - jogador.getX());
                int distanciaY = Math.abs(inimigo.getY() - jogador.getY());
                int distanciaTotal = distanciaX + distanciaY;
                if (distanciaTotal < menorDistancia || menorDistancia == 0) {
                    menorDistancia = distanciaTotal;
                    alvo = jogador;
                }
            }
            if (alvo != null) {
                inimigo.mover(alvo.getX(), alvo.getY(), alvo.getDirecao());
            }

        }

        atualizarInimigos();
    }

    // Funções relacionadas a itens
    public void atualizarItens() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarItens");
        respostaServidor.put("itens", mapa.getItens());
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    public void itemLevouDano(String idJogador, String id, int dano) throws IOException {
        mapa.itemLevouDano(id, dano);
        if (mapa.itemEstaVivo(id)){
            Jogador jogador = jogadores.get(idJogador);
            if (jogador != null) {
                String nomeItem = mapa.getItens().get(id).getNome();
                jogadorReceberAtributoExtra(jogador, nomeItem);
                mapa.removerItem(id);
            }
        }
        atualizarItens();
    }

    // Funções relacionadas a barreiras
    public void atualizarBarreiras() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarBarreiras");
        respostaServidor.put("barreiras", mapa.getBarreiras());
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    public void barreiraLevouDano(String id, int dano) throws IOException {
        mapa.barreiraLevouDano(id, dano);
        atualizarBarreiras();
    }

}
