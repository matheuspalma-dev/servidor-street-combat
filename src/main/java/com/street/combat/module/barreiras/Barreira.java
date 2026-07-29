package com.street.combat.module.barreiras;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Barreira {

    private final String tipo;
    private int vida;
    private int x;
    private int y;

    public Barreira(String tipo, String mapa) {
        this.tipo = tipo;

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
        this.y = new Random().nextInt((500 - 100) + 1) + 100;
    }

    public boolean estarMorto() {
        return this.vida <= 0;
    }

    public void levarDano(int dano) {
        this.vida -= dano;
    }
}
