package com.street.combat.module.mapa;

import com.street.combat.module.barreiras.Barreira;
import com.street.combat.module.itens.Item;
import com.street.combat.module.Inimigos.Inimigo;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Mapa {

    private final String nome;
    private Map<String, Inimigo> inimigos = new ConcurrentHashMap<>();
    private Map<String, Item> itens = new ConcurrentHashMap<>();
    private Map<String, Barreira> barreiras = new ConcurrentHashMap<>();
    private final boolean mapaLutaBoss;

    public Mapa(String nome, boolean mapaLutaBoss) {
        this.nome = nome;
        this.mapaLutaBoss = mapaLutaBoss;
        if (!mapaLutaBoss) {
            //carregarItens();
            //carregarBarreiras();
        } else{

        }
    }

    public void carregarInimigos(int grupo){
        switch (this.nome){
            case "cidade":
                int posicaoInicialBase = 1600;
                int larguraGrupo = 750;
                int espacoEntreGrupos = 600;
                int quantidadeInimigosNoGrupo = ThreadLocalRandom.current().nextInt(3, 6);
                int minX = posicaoInicialBase + grupo * (larguraGrupo + espacoEntreGrupos);
                int maxX = minX + larguraGrupo;
                int[] posicoesAliados = new int[quantidadeInimigosNoGrupo];
                for (int loop = 0; loop < quantidadeInimigosNoGrupo; loop++){
                    int sorteio = ThreadLocalRandom.current().nextInt(1, 4);
                    String id = UUID.randomUUID().toString();
                    Inimigo inimigo;

                    switch (sorteio) {
                        case 1:
                            inimigo = new Inimigo("david", posicoesAliados, minX, maxX);
                            break;
                        case 2:
                            inimigo = new Inimigo("raymond", posicoesAliados, minX, maxX);
                            break;
                        case 3:
                            inimigo = new Inimigo("rick", posicoesAliados, minX, maxX);
                            break;
                        case 4:
                            inimigo = new Inimigo("stella", posicoesAliados, minX, maxX);
                            break;
                        default:
                            inimigo = new Inimigo("david", posicoesAliados, minX, maxX);
                            break;
                    }

                    posicoesAliados[loop] = inimigo.getX();
                    inimigos.put(id, inimigo);
                }

                break;
            case "prisão":
            case "quintal":
        }
    }

    public void removerInimigo(String id){
        Inimigo inimigo = inimigos.get(id);
        if (inimigo != null) {
            inimigos.remove(id);
        }
    }


    public void inimigoLevouDano(String id, int dano){
        Inimigo inimigo = inimigos.get(id);
        if (inimigo != null) {
            inimigo.levarDano(dano);
            if (!inimigo.estaVivo()) {
                inimigos.remove(id);
            }
        }
    }

    public boolean semInimigos(){
        return inimigos.isEmpty();
    }

    public void carregarItens(){
        switch (this.nome){
            case "cidade":
                int QuantidadeItens = 10;
                int recuperarVida = 2;
                int danoExtra = 2;
                int escudoExtra = 1;
                int veneno = 2;
                int velocidadeExtra = 2;

                for (int i = 0; i < QuantidadeItens; i++){
                    String id = UUID.randomUUID().toString();
                    if (recuperarVida > 0){
                        itens.put(id, new Item("recuperarVida", "cidade"));
                        recuperarVida -= 1;
                    } else if (danoExtra > 0) {
                        itens.put(id, new Item("danoExtra", "cidade"));
                        danoExtra -= 1;
                    } else if (escudoExtra > 0) {
                        itens.put(id, new Item("escudoExtra", "cidade"));
                        escudoExtra -= 1;
                    } else if (veneno > 0) {
                        itens.put(id, new Item("veneno", "cidade"));
                        veneno -= 1;
                    } else {
                        itens.put(id, new Item("velocidadeExtra", "cidade"));
                    }
                }
                break;
            case "prisão":
            case "quintal":
        }
    }

    public void itemLevouDano(String id, int dano){
        Item item = itens.get(id);
        if (item != null) {
            item.levarDano(dano);
        }
    }

    public void removerItem(String id){
        Item item = itens.get(id);
        if (item != null) {
            itens.remove(id);
        }
    }

    public boolean itemEstaVivo(String id) {
        Item item = itens.get(id);
        return item.estaMorto();
    }

    public void carregarBarreiras(){
        switch (this.nome){
            case "cidade":
                int QuantidadeBarreiras = 4;

                for (int i = 0; i < QuantidadeBarreiras; i++){
                    String id = UUID.randomUUID().toString();
                    barreiras.put(id, new Barreira("barreira", "cidade"));
                }
                break;
            case "prisão":
            case "quintal":
        }
    }

    public void barreiraLevouDano(String id, int dano){
        Barreira barreira = barreiras.get(id);
        if (barreira != null) {
            barreira.levarDano(dano);
            if (barreira.estarMorto()){
                removerBarreira(id);
            }
        }
    }

    private void removerBarreira(String id){
        Barreira barreira = barreiras.get(id);
        if (barreira != null) {
            barreiras.remove(id);
        }
    }
}
