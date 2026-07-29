package com.street.combat.module.Inimigos;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Inimigo {

    private final String nome;
    private int vida;
    private final int dano;
    private final boolean boss;
    private final int pontosPorDerrotar;

    private int x;
    private int y;
    private String direcao;
    private int velocidade;

    public Inimigo(String nome) {
        this.nome = nome;
        this.direcao = "esquerda";

        switch (nome) {
            case "david":
                this.pontosPorDerrotar = 1;
                this.vida = 10;
                this.dano = 2;
                this.boss = false;
                this.velocidade = 2;
                break;
            case "raymond":
                this.pontosPorDerrotar = 1;
                this.vida = 8;
                this.dano = 4;
                this.boss = false;
                this.velocidade = 2;
                break;
            case "rick":
                this.pontosPorDerrotar = 1;
                this.vida = 9;
                this.dano = 3;
                this.boss = false;
                this.velocidade = 2;
                break;
            case "stella":
                this.pontosPorDerrotar = 1;
                this.vida = 6;
                this.dano = 6;
                this.boss = false;
                this.velocidade = 5;
                break;
            case "LucasLee":
                this.pontosPorDerrotar = 10;
                this.vida = 40;
                this.dano = 20;
                this.boss = true;
                this.velocidade = 1;
                break;
            default:
                this.pontosPorDerrotar = 0;
                this.vida = 0;
                this.dano = 0;
                this.boss = false;
                this.velocidade = 2;
                break;
        }

        this.x = new Random().nextInt((500 - 100) + 1) + 100;
        this.y = new Random().nextInt((500 - 100) + 1) + 100;
    }

    public void mover(int jogadorX, int jogadorY) {
        if (this.y < jogadorY){
            this.y += velocidade;
        } else if (this.y > jogadorY){
            this.y -= velocidade;
        }

        int distanciaX = Math.abs(this.x - jogadorX);

        if (this.x < jogadorX && distanciaX > 10){
            this.x += velocidade;
        } else if (this.x > jogadorX && distanciaX > 10){
            this.x -= velocidade;
        }

        atualizarDirecao(jogadorX);

        /*  testar depois essa logica
        double dx = jogadorX - x;
        double dy = jogadorY - y;

        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia > 0) {
            x += (dx / distancia) * velocidade;
            y += (dy / distancia) * velocidade;
        }
        */
    }

    private void moverAleatoriamente(){

    }

    private void atualizarDirecao(int jogadorX) {
        if (this.x < jogadorX) {
            this.direcao = "direita";
        } else if (this.x > jogadorX) {
            this.direcao = "esquerda";
        }
    }

    public void levarDano(int dano) {
        this.vida -= dano;
        if (this.vida < 0) {
            this.vida = 0;
        }
    }

    public boolean estaVivo() {
        return this.vida > 0;
    }

}
