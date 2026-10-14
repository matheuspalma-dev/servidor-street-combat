package com.street.combat.module.salas;

import com.street.combat.module.Inimigos.Inimigo;
import com.street.combat.module.jogador.Jogador;
import com.street.combat.module.mapa.Mapa;
import lombok.Getter;
import lombok.Setter;
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
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();
    private boolean[] gruposInimigosCarregados = {false, false, false, false, false, false};
    private Turno turno = Turno.SEM_TURNO;

    public Sala(String nomeMapa, String nomeCriador) throws IOException {
        this.id = nomeCriador;
        this.mapa = new Mapa(nomeMapa, false);
    }

    public void iniciarLoop(){
        if (executor.isShutdown() || executor == null){
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        executor.scheduleAtFixedRate
                (() -> {
                    try {
                        inimigoEscolherJogadorParaAtacar();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 1000, 50, TimeUnit.MILLISECONDS);
    }

    public void encerrarLoop() {
        executor.shutdownNow();
    }

    public void iniciarBatalhaBoss(){
        iniciarLoop();
    }

    public void iniciarBatalhaDoBoss() throws IOException {
        encerrarLoop();
        mapa = new Mapa("cidadeBoss", true);
        mapa.carregarBoss();
        turno = Turno.PRIMEIRO_TURNO;
        atualizarMapas();
        atualizarInimigos();
        reposicionarJogadores();
    }

    private void reposicionarJogadores(){
        for (Jogador jogador : jogadores.values()){
            jogador.reposicionar();
        }
    }

    // Funções relacionadas a salas
    public void adicionarJogador(WebSocketSession session, String id, String personagem, String nomeJogador, boolean novoJogador, String skin) throws IOException {
        Jogador jogador = new Jogador(personagem, id, this.mapa.getNome(), nomeJogador, skin);
        if (novoJogador){
            jogador.setX(200);
            jogador.setXInicio(500);
        }
        jogadores.put(session.getId(), jogador);
        sessions.put(id, session);
        atualizarJogadores();
        atualizarBarreiras();
        atualizarItens();
        atualizarMapas();
    }

    public void atualizarMapas() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarMapa");
        respostaServidor.put("mapa", mapa.getNome());
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
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
        if (jogador != null) {
            jogador.mover(x, y, direcao);
            int maiorX = maiorXJogador();
            adicionarInimigos(maiorX);
            atualizarJogadores();
        }
    }

    private void adicionarInimigos(int xJogador) throws IOException {
        if (!gruposInimigosCarregados[0] && xJogador >= 1100 && xJogador <= 2150){
            mapa.carregarInimigos(0);
            gruposInimigosCarregados[0] = true;
        } else if (!gruposInimigosCarregados[1] && xJogador >= 2350 && xJogador <= 3900){
            mapa.carregarInimigos(1);
            gruposInimigosCarregados[1] = true;
        } else if (!gruposInimigosCarregados[2] && xJogador >= 3850 && xJogador <= 5050) {
            mapa.carregarInimigos(2);
            gruposInimigosCarregados[2] = true;
        } else if (!gruposInimigosCarregados[3] && xJogador >= 5250 && xJogador <= 6600){
            mapa.carregarInimigos(3);
            gruposInimigosCarregados[3] = true;
        } else if (!gruposInimigosCarregados[4] && xJogador >= 5950 && xJogador <= 7950){
            mapa.carregarInimigos(4);
            gruposInimigosCarregados[4] = true;
        } else if (!gruposInimigosCarregados[5] && xJogador >= 7350 && xJogador <= 9300) {
            mapa.carregarInimigos(5);
            gruposInimigosCarregados[5] = true;
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
        final int distanciaMaxima = 1700;

        for (int i = 0; i < gruposInimigosCarregados.length; i++) {
            if (!gruposInimigosCarregados[i]) continue;

            int minX = posicaoInicialBase + i * (larguraGrupo + espacoEntreGrupos);
            int maxX = minX + larguraGrupo;

            if (maxX < xJogador - distanciaMaxima) {
                final int fMinX = minX;
                final int fMaxX = maxX;
                mapa.getInimigos().entrySet().removeIf(entry -> {
                    int ex = entry.getValue().getX();
                    return ex >= fMinX && ex <= fMaxX;
                });
                gruposInimigosCarregados[i] = false;
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

            if (inimigo.isBoss() && turno == Turno.SEGUNDO_TURNO) continue;

            Jogador alvo = null;
            int menorDistancia = 0;
            for (Jogador jogador : jogadores.values()) {
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
                inimigo.mover(alvo.getX(), alvo.getY(), alvo.isDirecao(), mapa.getBarreiras().values(), turno);
                if (inimigo.isAtacar()) {
                    inimigoVaiTentarGolpe(inimigo);
                    inimigosAtacando.add(inimigo);
                }
            }

            atualizarInimigos();
            for (Inimigo inimigoAtacando : inimigosAtacando){
                controlarAtaqueInimigo(inimigoAtacando);
            }


        }
    }

    private void controlarAtaqueInimigo(Inimigo inimigo) {
        inimigo.setAtacar(false);
        inimigo.setAtacando(true);
        inimigo.setPausaAtaque(true);
        if (inimigo.isBoss()){
            executor.schedule(() -> {
                inimigo.setAtacando(false);
                inimigo.setPausaAtaque(false);
            }, 3300, TimeUnit.MILLISECONDS);
        } else {
            executor.schedule(() -> {
                inimigo.setAtacando(false);
                inimigo.setPausaAtaque(false);
            }, 2300, TimeUnit.MILLISECONDS);
        }
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
            case "LucasLee":
                if (turno == Turno.PRIMEIRO_TURNO){
                    golpesDisponiveis = new String[]{"lucas_lee_ataque_1", "lucas_lee_ataque_2"};
                } else {
                    golpesDisponiveis = new String[]{"lucas_lee_ataque_1_skate", "lucas_lee_ataque_2_skate"};
                }
                break;
            default:
                golpesDisponiveis = new String[]{};
                break;
        }

        return golpesDisponiveis[random.nextInt(golpesDisponiveis.length)];
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

    public void itemLevouDano(int dano, String idItem) throws IOException {
        mapa.itemLevouDano(idItem, dano);
        atualizarItens();
    }

    public void jogadorRecuperouVida(String id, int vidaRecuperada) throws IOException {
        Jogador jogador = jogadores.get(id);
        if (jogador != null) {
            jogador.recuperarVida(vidaRecuperada);
            atualizarJogadores();
        }
    }

    public void iniciarBatalhaComBoss() throws IOException {
        iniciarBatalhaDoBoss();
    }

    public void danoNoBoss(String idBoss, int dano) throws IOException {
        String nomeDoBoss = mapa.getInimigos().get(idBoss).getNome();
        if (nomeDoBoss.equals("LucasLee")){
            if (mapa.getInimigos().get(idBoss).getVida() - dano <= 0) {
                mapa.danoNoBoss(idBoss, dano);
                atualizarInimigos();
                acabarPartida();
            }
            else if (mapa.getInimigos().get(idBoss).getVida() - dano <= 100){
                if (turno == Turno.PRIMEIRO_TURNO){
                    turno = Turno.SEGUNDO_TURNO;
                    atualizarTurno();
                    mapa.danoNoBoss(idBoss, dano);
                    atualizarInimigos();
                    bossSairDeCena(idBoss);
                } else {
                    mapa.danoNoBoss(idBoss, dano);
                    atualizarInimigos();
                }
            } else {
                mapa.danoNoBoss(idBoss, dano);
                atualizarInimigos();
            }
        }
    }
    public void acabarPartida() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "fimDaPartida");
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    public void pegouItem(String idItem) throws IOException {
        mapa.pegouItem(idItem);
        atualizarItens();
    }

    @Async
    public void bossSairDeCena(String id) throws IOException {
        while (mapa.getInimigos().get(id).getX() > -200){
            mapa.getInimigos().get(id).irParaFimDoMapa();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        mapa.adicionarInimigosBatalhaBoss1();
        atualizarInimigos();
    }

    public void inimigoTomouDano(String idInimigo, int dano) throws IOException {
        boolean deletouInimgo = mapa.inimigoLevouDano(idInimigo, dano);
        if (deletouInimgo) {
            if (turno == Turno.SEGUNDO_TURNO && mapa.getInimigos().size() == 1){
                turno = Turno.TERCEIRO_TURNO;
                atualizarTurno();
                mapa.adicionarInimigosBatalhaBoss1();
            } else if (turno == Turno.TERCEIRO_TURNO && mapa.getInimigos().size() == 1) {
                turno = Turno.QUARTO_TURNO;
                atualizarTurno();
            }
        }
    }

    public void atualizarTurno() throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "atualizarTurno");
        respostaServidor.put("turno", this.turno);
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

    public void bossSeDefendeu(String inimigoId) throws IOException {
        Map<String, Object> respostaServidor = new HashMap<>();
        respostaServidor.put("tipo", "inimigoSeDefender");
        respostaServidor.put("idInimigo", inimigoId);
        for(WebSocketSession session : sessions.values()){
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(tradutor.writeValueAsString(respostaServidor)));
            }
        }
    }

}
