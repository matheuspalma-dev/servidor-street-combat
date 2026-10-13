package com.street.combat.module.Inimigos;

import com.street.combat.module.barreiras.Barreira;
import com.street.combat.module.salas.Turno;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.abs;

@Getter
@Setter
public class Inimigo {

    private final String id;
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
    private int defesa;

    private boolean podeSeMover = true;
    private boolean atacar = false;
    private boolean atacando = false;
    private boolean pausaAtaque = false;
    private boolean ponta = false; // true = esquerda, false = direita

    private final Random random = new Random();

    public Inimigo(String nome, int[] posicoesAliados, int minX, int maxX, String id) {
        this.id = id;
        this.nome = nome;
        this.direcao = false; //direita
        this.quantidadeMovimentos = 5;

        switch (nome) {
            case "david":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 10;
                this.boss = false;
                this.velocidade = 10;
                this.defesa = 8;
                break;
            case "raymond":
                this.pontosPorDerrotar = 1;
                this.vida = 45;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 8;
                this.defesa = 10;
                break;
            case "rick":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 6;
                this.defesa = 8;
                break;
            case "stella":
                this.pontosPorDerrotar = 1;
                this.vida = 20;
                this.dano = 12;
                this.boss = false;
                this.velocidade = 10;
                this.defesa = 6;
                break;
            case "LucasLee":
                this.pontosPorDerrotar = 10;
                this.vida = 200;
                this.dano = 15;
                this.boss = true;
                this.velocidade = 8;
                this.defesa = 15;
                break;
            default:
                this.pontosPorDerrotar = 0;
                this.vida = 0;
                this.dano = 0;
                this.boss = false;
                this.velocidade = 2;
                break;
        }

        int posicaoX = xCorreto(minX, maxX);
        this.x = posicaoX;// ThreadLocalRandom.current().nextInt(1500, 4001);
        this.y = ThreadLocalRandom.current().nextInt(550, 580);
    }

    public Inimigo(String nome, int x, int y, String id) {
        this.id = id;
        this.nome = nome;
        this.direcao = true; //direita

        switch (nome) {
            case "david":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 10;
                this.boss = false;
                this.velocidade = 10;
                this.defesa = 8;
                break;
            case "raymond":
                this.pontosPorDerrotar = 1;
                this.vida = 45;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 8;
                this.defesa = 10;
                break;
            case "rick":
                this.pontosPorDerrotar = 1;
                this.vida = 30;
                this.dano = 8;
                this.boss = false;
                this.velocidade = 6;
                this.defesa = 8;
                break;
            case "stella":
                this.pontosPorDerrotar = 1;
                this.vida = 20;
                this.dano = 12;
                this.boss = false;
                this.velocidade = 10;
                this.defesa = 6;
                break;
            case "LucasLee":
                this.pontosPorDerrotar = 10;
                this.vida = 200;
                this.dano = 15;
                this.boss = true;
                this.velocidade = 8;
                this.defesa = 15;
                break;
            default:
                this.pontosPorDerrotar = 0;
                this.vida = 0;
                this.dano = 0;
                this.boss = false;
                this.velocidade = 2;
                break;
        }

        this.x = x;
        this.y = y;
    }

    public void seMoverAteAPonta(){

        int sorteio = ThreadLocalRandom.current().nextInt(1, 4);
        if (this.ponta){
            this.x -= velocidade;
        } else {
            this.x += velocidade;
        }

        if (sorteio == 1) {
            this.y -= velocidade / 2;
        } else if (sorteio == 2) {
            this.y += velocidade / 2;
        }

        if (this.y >= 710){
            this.y = 710;
        }

        if (this.y <= 600){
            this.y = 600;
        }

        if (this.x <= 10){
            this.ponta = false;
        } else if (this.x >= 1500){
            this.ponta = true;
        }
    }
    private int xCorreto(int minX, int maxX){
        return ThreadLocalRandom.current().nextInt(minX, maxX + 1);
    }

    public void serJogadoParaTras(int distancia, Collection<Barreira> barreiras){
        podeSeMover = false;
        if (this.direcao){
            if (podeSeMoverParaEsseX(this.x - distancia, barreiras)) {
                this.x -= distancia;
            }
        } else {
            if (podeSeMoverParaEsseX(this.x + distancia, barreiras)) {
                this.x += distancia;
            }
        }

        int sorteio = ThreadLocalRandom.current().nextInt(1, 3);
        if (sorteio == 1){
            y -= distancia / 2;
            if (y < 393){
                y = 393;
            }
        } else {
            y += distancia / 2;
            if (y > 608){
                y = 608;
            }
        }
    }

    public void mover(int jogadorX, int jogadorY, boolean direcaoJogador, Collection<Barreira> barreiras, Turno turno) {
        this.atacar = false;

        if (!podeSeMover || atacando) {
            return;
        }

        if (nome.equals("LucasLee") && turno == Turno.TERCEIRO_TURNO && boss) {
            seMoverAteAPonta();
            return;
        }

        atualizarDirecao(jogadorX);

        boolean moveuX = false;
        int distanciaX = abs(this.x - jogadorX);
        int distanciaY = abs(this.y - jogadorY);
        int distanciaTotal = distanciaX + distanciaY;


        if (this.boss){
            moveuX = isMoveuX(jogadorX, jogadorY, direcaoJogador, barreiras, moveuX, distanciaX, distanciaY);
        } else {
            if (turno == Turno.SEM_TURNO) {
                if (distanciaTotal > 400) {
                    moverAleatoriamente(barreiras);
                } else {
                    moveuX = isMoveuX(jogadorX, jogadorY, direcaoJogador, barreiras, moveuX, distanciaX, distanciaY);
                }
            } else {
                moveuX = isMoveuX(jogadorX, jogadorY, direcaoJogador, barreiras, moveuX, distanciaX, distanciaY);
            }
        }
    }

    private boolean isMoveuX(int jogadorX, int jogadorY, boolean direcaoJogador, Collection<Barreira> barreiras, boolean moveuX, int distanciaX, int distanciaY) {
        if (this.y < jogadorY) {
            if (abs(jogadorY - this.y) < 5 && jogadorX == this.x) {
                if (direcaoJogador) { // direção true (esquerda)
                    if (podeSeMoverParaEsseX(this.x + velocidade, barreiras)) {
                        this.x += velocidade;
                    }
                } else {
                    if (podeSeMoverParaEsseX(this.x - velocidade, barreiras)) {
                        this.x -= velocidade;
                    }
                }
                moveuX = true;
            } else {
                this.y += velocidade;
            }
        } else if (this.y > jogadorY) {
            if (abs(jogadorY - this.y) < 5 && jogadorX == this.x) {
                if (direcaoJogador) { // direção true (esquerda)
                    if (podeSeMoverParaEsseX(this.x + velocidade, barreiras)) {
                        this.x += velocidade;
                    }
                } else {
                    if (podeSeMoverParaEsseX(this.x - velocidade, barreiras)) {
                        this.x -= velocidade;
                    }
                }
                moveuX = true;
            } else {
                this.y -= velocidade;
            }
        }

        if (!moveuX) {
            if (distanciaX > 60) {
                if (this.x < jogadorX) {
                    if (podeSeMoverParaEsseX(this.x + velocidade, barreiras)) {
                        this.x += velocidade;
                    }
                } else if (this.x > jogadorX) {
                    if (podeSeMoverParaEsseX(this.x - velocidade, barreiras)) {
                        this.x -= velocidade;
                    }
                }
            } else if (distanciaX <= 60 && distanciaY <= 25) {
                if (!this.pausaAtaque && !this.atacando) {
                    this.atacar = true;
                    this.atacando = true;
                    this.pausaAtaque = true;
                }
            }
        }
        return moveuX;
    }

    private void moverAleatoriamente(Collection<Barreira> barreiras){
        if (quantidadeMovimentos <= 0){
            direcaoAleatoria = random.nextInt(4);
            quantidadeMovimentos = 5;
        }

        switch (direcaoAleatoria) {
            case 0: // cima
                this.y -= velocidade;
                if (this.y < 50) {
                    this.y = 50;
                }
                break;
            case 1: // baixo
                this.y += velocidade;
                if (this.y > 600) {
                    this.y = 600;
                }
                break;
            case 2: // esquerda
                if (podeSeMoverParaEsseX(this.x - velocidade, barreiras)) {
                    this.x -= velocidade;
                    if (this.x < 10) {
                        this.x = 10;
                    }
                }
                break;
            case 3: // direita
                if (podeSeMoverParaEsseX(this.x + velocidade, barreiras)) {
                    this.x += velocidade;
                    if (this.x > 9447) {
                        this.x = 9447;
                    }
                }
                break;
        }
        quantidadeMovimentos -= 1;
    }

    public boolean podeSeMoverParaEsseX(int x, Collection<Barreira> barreiras) {
        if (barreiras != null) {
            for (Barreira barreira : barreiras) {
                if (abs(x - barreira.getX()) <= 40 && abs(this.y - barreira.getY()) <= 40) {
                    return false;
                }
            }
        }
        return true;
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

    public void irParaFimDoMapa(){
        this.x -= velocidade;
    }

}
