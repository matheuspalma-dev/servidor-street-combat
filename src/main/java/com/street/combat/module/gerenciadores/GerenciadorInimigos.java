package com.street.combat.module.gerenciadores;

import com.street.combat.module.Inimigos.Inimigo;
import com.street.combat.module.jogador.AtaqueRamona;
import com.street.combat.module.jogador.AtaqueScott;
import com.street.combat.module.mapa.Mapa;
import com.street.combat.module.salas.Sala;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class GerenciadorInimigos {

    private  final GerenciadorSalas gerenciadorSalas;

    public GerenciadorInimigos(GerenciadorSalas gerenciadorSalas) {
        this.gerenciadorSalas = gerenciadorSalas;
    }

    @Async
    public void levouDano(WebSocketSession session, String idInimigo, Object golpe, int danoDado) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                int distanciaParaSerJogadoParaTras = calcularDistanciaParaSerJogadoParaTras(golpe);
                sala.inimigoTomouDano(idInimigo, danoDado);
                jogarInimigoParaTras(mapa, sala, idInimigo, distanciaParaSerJogadoParaTras);
            }
        }
    }

    @Async
    public void jogarInimigoParaTras(Mapa mapa, Sala sala, String idInimigo, int distanciaParaSerJogadoParaTras) throws IOException {
        Inimigo inimigo = mapa.getInimigos().get(idInimigo);
        if (inimigo != null){
            for (int i = 0; i < 4; i++){
                inimigo.serJogadoParaTras(distanciaParaSerJogadoParaTras, mapa.getBarreiras().values());
                sala.atualizarInimigos();
                try{
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            inimigo.setPodeSeMover(true);
        }
    }

    private int calcularDistanciaParaSerJogadoParaTras(Object golpe) {
        if (golpe instanceof AtaqueScott){
            if (golpe == AtaqueScott.CHUTE_BAIXO){
                return 26; // 120
            } else if (golpe == AtaqueScott.SOCO_DIRETO){
                return 12; // mais fraco 30
            } else { // demais golpes
                return 20; // mais longe 90
            }
        } else if (golpe instanceof AtaqueRamona){
            if (golpe == AtaqueRamona.MARTELO_MEDIO_ALCANCE){
                return 28; // 120
            } else if (golpe == AtaqueRamona.SOCO){
                return 9; // mais fraco 30
            } else { // demais golpes
                return 20; // mais longe 90
            }
        } else {
            System.out.println("Golpe nem chegou aqui");
        }
        return 0;
    }

    public void barreiraLevouDano(WebSocketSession session, String idBarreira, int dano) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.barreiraLevouDano(idBarreira, dano);
                sala.atualizarBarreiras();
            }
        }
    }

    public void itemLevouDano(int dano, String idItem) throws IOException {
        for (Sala sala : gerenciadorSalas.getSalasOnline().values()) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.itemLevouDano(dano,idItem);
                sala.atualizarItens();
            }
        }
    }

    public void pegouItem(WebSocketSession session, String idItem) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.pegouItem(idItem);
                sala.atualizarItens();
            }
        }
    }

    public void danoNoBoss(WebSocketSession session, String idBoss, int dano) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.danoNoBoss(idBoss, dano);
                sala.atualizarInimigos();
            }
        }
    }

    public void bossSeDefender(WebSocketSession session, String inimigoId) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                sala.bossSeDefendeu(inimigoId);
            }
        }
    }
}
