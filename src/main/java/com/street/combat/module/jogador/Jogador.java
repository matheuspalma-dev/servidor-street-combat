package com.street.combat.module.jogador;


import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
public class Jogador {
    private String id;
    private final String nomeJogador;
    private final String personagem;
    private boolean direcao;
    private int vida;
    private int vidaMaxima;
    private int defesa;
    private int x;
    private int y;
    private int dano;
    private Set<AtributosExtra> atributosExtras = EnumSet.noneOf(AtributosExtra.class);

    public Jogador(String personagem, String id, String mapa, String nomeJogador) {
        this.id = id;
        this.nomeJogador = nomeJogador;
        this.personagem = personagem;
        this.direcao = false; // false = direita, true = esquerda
        this.x = 50;
        this.y = 550;

        switch (mapa){
            case "cidade":
                this.vida = 50;
                this.vidaMaxima = 50;
                this.defesa = 10;
                this.dano = 10;
                break;
            case "prisão":
                this.vida = 34;
                this.vidaMaxima = 34;
                this.defesa = 20;
                this.dano = 6;
                break;
            case "quintal":
                this.vida = 42;
                this.vidaMaxima = 42;
                this.defesa = 30;
                this.dano = 8;
                break;
            default:
                this.vida = 20;
                this.vidaMaxima = 20;
                this.dano = 5;
                break;
        }
    }

    public void mover(int x, int y, boolean direcao){
        this.x = x;
        this.y = y;
        this.direcao = direcao;
    }

    public void levarDano(int dano){
        this.vida -= dano;
        if (this.vida < 0){
            this.vida = 0;
        }
    }

    public boolean estaVivo(){
        return this.vida > 0;
    }

    public void adicionarAtributoExtra(AtributosExtra atributoExtra){
        this.atributosExtras.add(atributoExtra);
    }

    public void removerAtributoExtra(AtributosExtra atributoExtra){
        this.atributosExtras.remove(atributoExtra);
    }

    public void curar(){
        this.vida += 5;
        if (this.vida > this.vidaMaxima) {
            this.vida = this.vidaMaxima;
        }
    }
}
