package com.street.combat.module.ranking;

import com.street.combat.module.ranking.dto.RankingResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/mapa1")
    public ResponseEntity<List<RankingResponseDTO>> rankingMapa1(){
        return ResponseEntity.ok(rankingService.rankingMapa1());
    }

    @GetMapping("/mapa2")
    public ResponseEntity<List<RankingResponseDTO>> rankingMapa2(){
        return ResponseEntity.ok(rankingService.rankingMapa2());
    }

    @GetMapping("/mapa3")
    public ResponseEntity<List<RankingResponseDTO>> rankingMapa3(){
        return ResponseEntity.ok(rankingService.rankingMapa3());
    }

    @GetMapping("/pvp")
    public ResponseEntity<List<RankingResponseDTO>> rankingPVP(){
        return ResponseEntity.ok(rankingService.rankingPVP());
    }
}
