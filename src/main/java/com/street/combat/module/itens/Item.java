package com.street.combat.module.itens;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Item {

    private final String nome;
    private final String tipo;
    private int vida;
    private final int x;
    private final int y;

    public Item(String nome, String mapa) {
        this.nome = nome;
        this.tipo = "lixeira";
        switch (mapa){
            case "cidade":
                this.vida = 10;
                break;
            case "prisão":
                this.vida = 15;
                break;
            case "quintal":
                this.vida = 22;
                break;
            default:
                this.vida = 9;
                System.out.println("Algo deu errado, mapa não encontrado");
        }

        this.x = new Random().nextInt((500 - 100) + 1) + 100;
        this.y = new Random().nextInt( (500 - 100) + 1) + 100;
    }

    public void levarDano(int dano) {
        this.vida -= dano;
    }

    public boolean estaMorto() {
        return this.vida <= 0;
    }
}
