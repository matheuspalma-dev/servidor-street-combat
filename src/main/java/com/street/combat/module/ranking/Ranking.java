package com.street.combat.module.ranking;

import com.street.combat.module.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ranking")
@Getter
@Setter
public class Ranking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "jogador_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario jogador;
    @Column(name = "pontos_mapa_1", nullable = false)
    private int pontos_mapa_1;
    @Column(name = "pontos_mapa_2", nullable = false)
    private int pontos_mapa_2;
    @Column(name = "pontos_mapa_3", nullable = false)
    private int pontos_mapa_3;
    @Column(name = "pontos_pvp", nullable = false)
    private int pontos_pvp;
}
