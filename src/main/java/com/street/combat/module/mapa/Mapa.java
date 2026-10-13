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
            carregarItens();
            carregarBarreiras();
        }
    }

    public void carregarBoss(){
        switch (this.nome){
            case "cidadeBoss":
                String id = UUID.randomUUID().toString();
                Inimigo boss = new Inimigo("LucasLee", 800, 650, id);
                inimigos.put(id, boss);
                break;
            case "prisão":
            case "quintal":
        }
    }

    public void carregarInimigos(int grupo){
        switch (this.nome){
            case "cidade":
                int posicaoInicialBase = 1600;
                int larguraGrupo = 740;
                int espacoEntreGrupos = 600;
                int quantidadeInimigosNoGrupo = ThreadLocalRandom.current().nextInt(5, 8);
                int minX = posicaoInicialBase + grupo * (larguraGrupo + espacoEntreGrupos);
                int maxX = minX + larguraGrupo;
                int[] posicoesAliados = new int[quantidadeInimigosNoGrupo];
                for (int loop = 0; loop < quantidadeInimigosNoGrupo; loop++){
                    int sorteio = ThreadLocalRandom.current().nextInt(1, 4);
                    String id = UUID.randomUUID().toString();
                    Inimigo inimigo;

                    switch (sorteio) {
                        case 1:
                            inimigo = new Inimigo("david", posicoesAliados, minX, maxX, id);
                            break;
                        case 2:
                            inimigo = new Inimigo("raymond", posicoesAliados, minX, maxX, id);
                            break;
                        case 3:
                            inimigo = new Inimigo("rick", posicoesAliados, minX, maxX, id);
                            break;
                        case 4:
                            inimigo = new Inimigo("stella", posicoesAliados, minX, maxX, id);
                            break;
                        default:
                            inimigo = new Inimigo("david", posicoesAliados, minX, maxX, id);
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

    public boolean inimigoLevouDano(String id, int dano){
        Inimigo inimigo = inimigos.get(id);
        if (inimigo != null) {
            inimigo.levarDano(dano);
            if (!inimigo.estaVivo()) {
                inimigos.remove(id);
                return true;
            }
        }
        return false;
    }

    public void carregarItens(){
        switch (this.nome){
            case "cidade":
                int quantidadeDeGrupos = 5;
                int posicaoInicialBase = 1650;
                int larguraGrupo = 650;
                int espacoEntreGrupos = 620;
                for (int grupo = 1; grupo < quantidadeDeGrupos; grupo++) {
                    int quantidadeItens = ThreadLocalRandom.current().nextInt(0, 5);
                    int[] posicoesItens = new int[quantidadeItens];
                    for (int item = 0; item < quantidadeItens; item++) {
                        String id = UUID.randomUUID().toString();
                        int minX = posicaoInicialBase + grupo * (larguraGrupo + espacoEntreGrupos);
                        int maxX = minX + larguraGrupo;
                        int posicaoX = ThreadLocalRandom.current().nextInt(minX, maxX);
                        for (Barreira barreira : barreiras.values()){
                            if (posicaoX >= barreira.getX() && posicaoX <= barreira.getX() + 30) {
                                posicaoX = ThreadLocalRandom.current().nextInt(minX, maxX);
                            }
                        }
                        posicoesItens[item] = posicaoX;
                        int sorteioTipo = ThreadLocalRandom.current().nextInt(1, 101);
                        String skin = sortearSkinItem(sorteioTipo);
                        String tipo = skin.split("_")[0];
                        Item novoItem = new Item(tipo, "cidade", posicaoX, skin);
                        itens.put(id, novoItem);
                    }
                }
                break;
            case "prisão":
            case "quintal":
        }
    }

    private String sortearSkinItem(int tipo_item) {
        if (tipo_item >= 1 && tipo_item <= 20) {
            // agua_1 ate agua_4
            int variante = ThreadLocalRandom.current().nextInt(1, 5);
            return "agua_" + variante;
        } else if (tipo_item > 20 && tipo_item <= 39) {
            // comida_fraca_1 ate comida_fraca_18
            int variante = ThreadLocalRandom.current().nextInt(1, 19);
            return "comida_fraca_" + variante;
        } else if (tipo_item > 39 && tipo_item <= 45) {
            // veneno_1 ate veneno_4
            int variante = ThreadLocalRandom.current().nextInt(1, 5);
            return "veneno_" + variante;
        } else if (tipo_item > 45 && tipo_item <= 58) {
            // moeda_1 ate moeda_3
            int variante = ThreadLocalRandom.current().nextInt(1, 4);
            return "moeda_" + variante;
        } else if (tipo_item > 58 && tipo_item <= 68) {
            // comida_1 ate comida_30
            int variante = ThreadLocalRandom.current().nextInt(1, 31);
            return "comida_" + variante;
        } else if (tipo_item > 68 && tipo_item <= 73) {
            // pocao_regeneracao (sem variante)
            return "pocao_regeneracao";
        } else if (tipo_item > 73 && tipo_item <= 78) {
            // pocao_ataque (sem variante)
            return "pocao_ataque";
        } else if (tipo_item > 78 && tipo_item <= 83) {
            // pocao_defesa (sem variante)
            return "pocao_defesa";
        } else if (tipo_item > 83 && tipo_item <= 88) {
            // pocao_velocidade (sem variante)
            return "pocao_velocidade";
        } else if (tipo_item > 88 && tipo_item <= 95) {
            // mel_1 ate mel_8
            int variante = ThreadLocalRandom.current().nextInt(1, 9);
            return "mel_" + variante;
        } else if (tipo_item > 95 && tipo_item <= 100) {
            // torta_1 ate torta_3
            int variante = ThreadLocalRandom.current().nextInt(1, 4);
            return "torta_" + variante;
        } else {
            // fallback: moeda_1 ate moeda_3
            int variante = ThreadLocalRandom.current().nextInt(1, 4);
            return "moeda_" + variante;
        }
    }

    public void carregarBarreiras(){
        switch (this.nome){
            case "cidade":
                int quantidadeGruposBarreiras = 5;
                int quantidadeBarreirasPorGrupo = 4;

                int posicaoInicialBase = 1300;
                int espacoEntreGrupos = 1100;

                for (int i = 0; i < quantidadeGruposBarreiras; i++){
                    for (int b = 0; b < quantidadeBarreirasPorGrupo; b++) {
                        String id = UUID.randomUUID().toString();
                        int minX = posicaoInicialBase + i * (espacoEntreGrupos);
                        barreiras.put(id, new Barreira("barreira", "cidade", minX, b, id));
                    }
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

    public void itemLevouDano(String id, int dano){
        Item item = itens.get(id);
        if (item != null) {
            item.levarDano(dano);
        }
    }

    public void pegouItem(String id){
        Item item = itens.get(id);
        if (item != null) {
            itens.remove(id);
        }
    }

    public void danoNoBoss(String id, int dano){
        Inimigo boss = inimigos.get(id);
        if (boss != null) {
            boss.levarDano(dano);
            if (!boss.estaVivo()) {
                inimigos.remove(id);
            }
        }
    }

    public void adicionarInimigosBatalhaBoss1(){
        for (int i = 0; i < 6; i++){
            int sorteio = ThreadLocalRandom.current().nextInt(1, 4);
            String id = UUID.randomUUID().toString();
            Inimigo inimigo;
            int x = i >= 3? 1200: -100;
            int y;

            if (i == 1 || i == 4){
                y = 515;
            } else if (i == 2 || i == 5){
                y = 580;
            } else {
                y = 640;
            }
            inimigo = switch (sorteio) {
                case 1 -> new Inimigo("david", x, y, id);
                case 2 -> new Inimigo("raymond", x, y, id);
                case 3 -> new Inimigo("rick", x, y, id);
                case 4 -> new Inimigo("stella", x, y, id);
                default -> new Inimigo("david", x, y, id);
            };

            inimigos.put(id, inimigo);
        }
    }
}
