package com.street.combat.module.itens;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Item {

    private final String nome;
    private int vida;
    private String skin;
    private String lixeira;
    private final int x;
    private final int y;
    private int defesa;

    public Item(String nome, String mapa, int x, String skin) {
        this.nome = nome;
        this.skin = skin;
        switch (mapa){
            case "cidade":
                this.vida = 10;
                this.defesa = 5;
                break;
            case "prisão":
                this.vida = 15;
                this.defesa = 10;
                break;
            case "quintal":
                this.vida = 22;
                this.defesa = 13;
                break;
            default:
                this.vida = 9;
                System.out.println("Algo deu errado, mapa não encontrado");
        }

        this.x = x;
        int sorteioY = ThreadLocalRandom.current().nextInt(1, 3);
        if (sorteioY == 1) {
            this.y = 640;
        } else {
            this.y = 470;
        }

        int sorteioLixeira = ThreadLocalRandom.current().nextInt(1, 3);
        if (sorteioLixeira == 1) {
            this.lixeira = "lata_1";
        } else {
            this.lixeira = "lata_2";
        }

    }

    public void levarDano(int dano) {
        this.vida -= dano;
    }

    public boolean estaMorto() {
        return this.vida <= 0;
    }
}
