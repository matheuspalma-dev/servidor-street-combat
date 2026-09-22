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
    public void levouDano(WebSocketSession session, String idInimigo, Object golpe) throws IOException {
        Sala sala = gerenciadorSalas.getIdSalaDoJogador(session);
        if (sala != null) {
            int danoDoJogador = sala.danoDoJogador(session.getId());
            Mapa mapa = sala.getMapa();
            if (mapa != null) {
                int distanciaParaSerJogadoParaTras = calcularDistanciaParaSerJogadoParaTras(golpe);
                mapa.inimigoLevouDano(idInimigo, danoDoJogador);
                jogarInimigoParaTras(mapa, sala, idInimigo, distanciaParaSerJogadoParaTras);
            }
        }
    }

    @Async
    public void jogarInimigoParaTras(Mapa mapa, Sala sala, String idInimigo, int distanciaParaSerJogadoParaTras) throws IOException {
        Inimigo inimigo = mapa.getInimigos().get(idInimigo);
        if (inimigo != null){
            for (int i = 0; i < 4; i++){
                inimigo.serJogadoParaTras(distanciaParaSerJogadoParaTras);
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

            inimigo.setPodesSeMover(true);
        }
    }

    private int calcularDistanciaParaSerJogadoParaTras(Object golpe) {
        if (golpe instanceof AtaqueScott){
            if (golpe == AtaqueScott.CHUTE_BAIXO){
                return 60; // 120
            } else if (golpe == AtaqueScott.SOCO_DIRETO){
                return 18; // mais fraco 30
            } else { // demais golpes
                return 46; // mais longe 90
            }
        } else if (golpe instanceof AtaqueRamona){
            if (golpe == AtaqueRamona.MARTELO_MEDIO_ALCANCE){
                return 23; // 120
            } else if (golpe == AtaqueRamona.SOCO){
                return 8; // mais fraco 30
            } else { // demais golpes
                return 30; // mais longe 90
            }
        } else {
            System.out.println("Golpe nem chegou aqui");
        }
        return 0;
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
