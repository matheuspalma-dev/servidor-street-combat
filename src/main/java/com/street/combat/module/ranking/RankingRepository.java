package com.street.combat.module.ranking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    public Optional<Ranking> findByJogadorId(Long id);

    public boolean existsByJogadorId(Long jogadorId);
}
