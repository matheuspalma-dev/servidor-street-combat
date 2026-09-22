package com.street.combat.module.salas;

import com.street.combat.module.Inimigos.AtaquesInimigoMapaCidade;
import com.street.combat.module.Inimigos.Inimigo;
import com.street.combat.module.jogador.AtaqueRamona;
import com.street.combat.module.jogador.AtaqueScott;
import com.street.combat.module.jogador.AtributosExtra;
import com.street.combat.module.jogador.Jogador;
import com.street.combat.module.mapa.Mapa;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
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

    private String id;
    //depois eu removo
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Jogador> jogadores = new ConcurrentHashMap<>();
    private Mapa mapa;
    private final ObjectMapper tradutor = new ObjectMapper();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();
    private boolean[] gruposCarregados = {false, false, false, false, false, false};

    @SneakyThrows
    public Sala(String nomeMapa, String nomeCriador) {
        this.id = nomeCriador;
        this.mapa = new Mapa(nomeMapa, false);
    }

    public void iniciarLoop(){
        executor.scheduleAtFixedRate
                (() -> {
                    try {
                        inimigoEscolherJogadorParaAtacar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 1000, 50, TimeUnit.MILLISECONDS);
    }

    public void iniciarBatalhaDoBoss(){
        mapa = new Mapa(mapa.getNome(), true);
    }

    // Funções relacionadas a salas
    public void adicionarJogador(WebSocketSession session, String id, String personagem, String nomeJogador, boolean novoJogador) throws IOException {
        Jogador jogador = new Jogador(personagem, id, this.mapa.getNome(), nomeJogador);
        if (novoJogador){
            jogador.setX(200);
        }
        jogadores.put(session.getId(), jogador);
        sessions.put(id, session);
        atualizarJogadores();
        //atualizarItens();
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

    public void removerDaSala(String id) throws IOException {
        jogadores.remove(id);
        sessions.remove(id);
        atualizarJogadores();
    }

    public boolean salaVazia() {
        return jogadores.isEmpty();
    }

    // funções relacionadas a jogadores
    public void jogadorAndou(String id, int x, int y, boolean direcao) throws IOException {
        Jogador jogador = jogadores.get(id);
        jogador.mover(x, y, direcao);
        int maiorX = maiorXJogador();
        adicionarInimigos(maiorX);
        atualizarJogadores();
    }

    private void adicionarInimigos(int xJogador) throws IOException {
        if (!gruposCarregados[0] && xJogador >= 1100 && xJogador <= 2150){
            mapa.carregarInimigos(0);
            gruposCarregados[0] = true;
        } else if (!gruposCarregados[1] && xJogador >= 2750 && xJogador <= 3500){
            mapa.carregarInimigos(1);
            gruposCarregados[1] = true;
        } else if (!gruposCarregados[2] && xJogador >= 4100 && xJogador <= 4850) {
            mapa.carregarInimigos(2);
            gruposCarregados[2] = true;
        } else if (!gruposCarregados[3] && xJogador >= 5450 && xJogador <= 6200){
            mapa.carregarInimigos(3);
            gruposCarregados[3] = true;
        } else if (!gruposCarregados[4] && xJogador >= 6800 && xJogador <= 7550){
            mapa.carregarInimigos(4);
            gruposCarregados[4] = true;
        } else if (!gruposCarregados[5] && xJogador >= 8150 && xJogador <= 8900) {
            mapa.carregarInimigos(5);
            gruposCarregados[5] = true;
        }

        int menorX = menorXJogador();
        removerInimigos(menorX);
        atualizarInimigos();
    }

    private int maiorXJogador(){
        int maiorX = 0;
        for (Jogador j : jogadores.values()){
            if (j.getX() > maiorX){
                maiorX = j.getX();
            }
        }
        return maiorX;
    }

    private int menorXJogador(){
        int menorX = 20000;
        for (Jogador j : jogadores.values()){
            if (j.getX() < menorX){
                menorX = j.getX();
            }
        }
        return menorX;
    }

    private void removerInimigos(int xJogador) {
        final int posicaoInicialBase = 1600;
        final int larguraGrupo = 750;
        final int espacoEntreGrupos = 600;
        final int distanciaMaxima = 2000;

        for (int i = 0; i < gruposCarregados.length; i++) {
            if (!gruposCarregados[i]) continue;

            int minX = posicaoInicialBase + i * (larguraGrupo + espacoEntreGrupos);
            int maxX = minX + larguraGrupo;

            if (maxX < xJogador - distanciaMaxima) {
                final int fMinX = minX;
                final int fMaxX = maxX;
                mapa.getInimigos().entrySet().removeIf(entry -> {
                    int ex = entry.getValue().getX();
                    return ex >= fMinX && ex <= fMaxX;
                });
                gruposCarregados[i] = false;
            }
        }
    }


    public void jogadorTentouGolpe(String id, Object golpe) throws IOException {
        Jogador jogador = jogadores.get(id);
        if (jogador != null){
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

    public void jogadorLevouDano(String id, int dano) throws IOException {
        Jogador jogador = jogadores.get(id);
        if (jogador != null) {
            jogador.levarDano(dano);
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


    public void inimigoEscolherJogadorParaAtacar() throws IOException {
        List<Inimigo> inimigosAtacando = new ArrayList<>();
        for (Inimigo inimigo : mapa.getInimigos().values()){
            if (!inimigo.estaVivo()) continue;

            Jogador alvo = null;
            int menorDistancia = 0;
            for (Jogador jogador : jogadores.values()){
                if (!jogador.estaVivo()) continue;

                int distanciaX = Math.abs(inimigo.getX() - jogador.getX());
                int distanciaY = Math.abs(inimigo.getY() - jogador.getY());
                int distanciaTotal = distanciaX + distanciaY;
                if (distanciaTotal < menorDistancia || menorDistancia == 0) {
                    menorDistancia = distanciaTotal;
                    alvo = jogador;
                }
            }
            if (alvo != null) {
                inimigo.mover(alvo.getX(), alvo.getY(), alvo.isDirecao());
                if (inimigo.isAtacar()){
                    inimigoVaiTentarGolpe(inimigo);
                    inimigosAtacando.add(inimigo);
                }
            }

        }

        atualizarInimigos();
        for (Inimigo inimigo : inimigosAtacando){
            controlarAtaqueInimigo(inimigo);
        }
    }

    private void controlarAtaqueInimigo(Inimigo inimigo) {
        inimigo.setAtacar(false);
        inimigo.setAtacando(true);
        inimigo.setPausaAtaque(true);
        executor.schedule(() -> {
            inimigo.setAtacando(false);
            inimigo.setPausaAtaque(false);
        }, 1800, TimeUnit.MILLISECONDS);
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

    public void inimigoVaiTentarGolpe(Inimigo inimigo) throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "inimigoVaiTentarGolpe");
        respostaServidor.put("idInimigo", mapa.getInimigos().entrySet().stream()
                .filter(entry -> entry.getValue().equals(inimigo))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null));
        respostaServidor.put("golpe", descobrirAtaqueInimigo(inimigo.getNome()));
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    private String descobrirAtaqueInimigo(String nomeInimigo){
        String[] golpesDisponiveis;
        switch (nomeInimigo){
            case "david":
                golpesDisponiveis = new String[]{"david_golpe_1", "david_golpe_2", "david_golpe_3"};
                break;
            case "raymond":
                golpesDisponiveis = new String[]{"raymond_golpe_1", "raymond_golpe_2", "raymond_golpe_3"};
                break;
            case "rick":
                golpesDisponiveis = new String[]{"rick_golpe_1"};
                break;
            case "stella":
                golpesDisponiveis = new String[]{"stella_golpe_1"};
                break;
            default:
                golpesDisponiveis = new String[]{};
                break;
        }

        return golpesDisponiveis[random.nextInt(golpesDisponiveis.length)];
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
