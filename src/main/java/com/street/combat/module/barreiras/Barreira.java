package com.street.combat.module.barreiras;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Barreira {

    private final String id;
    private final String tipo;
    private int vida;
    private int x;
    private int y;
    private int defesa;

    public Barreira(String tipo, String mapa, int x, int Grupoy, String id) {
        this.id = id;
        this.tipo = tipo;

        switch (mapa){
            case "cidade":
                this.vida = 30;
                this.defesa = 6;
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

        this.x = x;
        if (Grupoy == 0){
            this.y = 460;
        } else if (Grupoy == 1) {
            this.y = 515;
        } else if (Grupoy == 2) {
            this.y = 580;
        } else {
            this.y = 640;
        }
    }

    public boolean estarMorto() {
        return this.vida <= 0;
    }

    public void levarDano(int dano) {
        this.vida -= dano;
    }
}
