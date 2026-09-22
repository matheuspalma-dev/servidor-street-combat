package com.street.combat.module.Inimigos;

import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.abs;

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
    private boolean direcao; // false = direita, true = esquerda
    private int velocidade;
    private int quantidadeMovimentos;
    private int direcaoAleatoria;

    private boolean podesSeMover = true;
    private boolean atacar = false;
    private boolean atacando = false;
    private boolean pausaAtaque = false;

    private final Random random = new Random();

    public
    Inimigo(String nome, int[] posicoesAliados, int minX, int maxX) {
        this.nome = nome;
        this.direcao = false; //direita
        this.quantidadeMovimentos = 5;

        switch (nome) {
            case "david":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 10;
                this.boss = false;
                this.velocidade = 12;
                break;
            case "raymond":
                this.pontosPorDerrotar = 1;
                this.vida = 45;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 8;
                break;
            case "rick":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 6;
                break;
            case "stella":
                this.pontosPorDerrotar = 1;
                this.vida = 20;
                this.dano = 12;
                this.boss = false;
                this.velocidade = 10;
                break;
            case "LucasLee":
                this.pontosPorDerrotar = 10;
                this.vida = 200;
                this.dano = 10;
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

        boolean podeEsseX;
        int posicaoX;
        do {
            posicaoX = xCorreto(minX, maxX);
            podeEsseX = podeEsseX(posicoesAliados, posicaoX);
        } while(!podeEsseX);
        this.x = posicaoX;// ThreadLocalRandom.current().nextInt(1500, 4001);
        this.y = ThreadLocalRandom.current().nextInt(550, 580);
    }

    private boolean podeEsseX(int[] posicoes, int posicaoX){
        for (int posicao : posicoes){
            if (posicaoX == posicao){
                return false;
            } else if (abs(posicaoX - posicao) < 90){
                return false;
            }
        }

        return true;
    }
    private int xCorreto(int minX, int maxX){
        return ThreadLocalRandom.current().nextInt(minX, maxX + 1);
    }

    public void serJogadoParaTras(int distancia){
        podesSeMover = false;
        if (this.direcao){
            x -= distancia;
        } else {
            x += distancia;
        }

        int sorteio = ThreadLocalRandom.current().nextInt(1, 3);
        if (sorteio == 1){
            y -= distancia / 2;
        } else {
            y += distancia / 2;
        }
    }

    public void mover(int jogadorX, int jogadorY, boolean direcaoJogador) {
        this.atacar = false;

        if (!podesSeMover || atacando) {
            atualizarDirecao(jogadorX);
            return;
        }

        atualizarDirecao(jogadorX);

        boolean moveuX = false;
        int distanciaX = abs(this.x - jogadorX);
        int distanciaY = abs(this.y - jogadorY);
        int distanciaTotal = distanciaX + distanciaY;

        if (distanciaTotal > 400) {
            moverAleatoriamente();
        } else {
            if (this.y < jogadorY) {
                if (abs(jogadorY - this.y) < 5 && jogadorX == this.x) {
                    if (direcaoJogador) { // direção true (esquerda)
                        this.x += velocidade;
                    } else {
                        this.x -= velocidade;
                    }
                    moveuX = true;
                } else {
                    this.y += velocidade;
                }
            } else if (this.y > jogadorY) {
                if (abs(jogadorY - this.y) < 5 && jogadorX == this.x) {
                    if (direcaoJogador) { // direção true (esquerda)
                        this.x += velocidade;
                    } else {
                        this.x -= velocidade;
                    }
                    moveuX = true;
                } else {
                    this.y -= velocidade;
                }
            }

            if (!moveuX) {
                if (distanciaX > 60) {
                    if (this.x < jogadorX) {
                        this.x += velocidade;
                    } else if (this.x > jogadorX) {
                        this.x -= velocidade;
                    }
                } else if (distanciaX <= 60 && distanciaY <= 25) {
                    if (!this.pausaAtaque && !this.atacando) {
                        this.atacar = true;
                        this.atacando = true;
                        this.pausaAtaque = true;
                    }
                }
            }
        }
    }

    private void moverAleatoriamente(){
        if (quantidadeMovimentos <= 0){
            direcaoAleatoria = random.nextInt(4);
            quantidadeMovimentos = 5;
        }

        switch (direcaoAleatoria) {
            case 0: // cima
                this.y -= velocidade;
                break;
            case 1: // baixo
                this.y += velocidade;
                break;
            case 2: // esquerda
                this.x -= velocidade;
                break;
            case 3: // direita
                this.x += velocidade;
                break;
        }
        quantidadeMovimentos -= 1;
    }

    private void atualizarDirecao(int jogadorX) {
        if (this.x < jogadorX) {
            this.direcao = true; // direita
        } else if (this.x > jogadorX) {
            this.direcao = false; // esquerda
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
