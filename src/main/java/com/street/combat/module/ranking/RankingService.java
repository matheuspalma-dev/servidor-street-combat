package com.street.combat.module.ranking;

import com.street.combat.module.ranking.dto.RankingResponseDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public List<RankingResponseDTO> rankingMapa1() {
        List<Ranking> ranking = new ArrayList<>();
        ranking.addAll(rankingRepository.findAll());

        List<RankingResponseDTO> rankingResponseDTOList = new ArrayList<>(ranking.size());
        for (Ranking r : ranking) {
            RankingResponseDTO rankingResponseDTO = devolverRanking(r, 1);
            rankingResponseDTOList.add(rankingResponseDTO);
        }

        return rankingResponseDTOList;
    }

    public List<RankingResponseDTO> rankingMapa2() {
        List<Ranking> ranking = new ArrayList<>();
        ranking.addAll(rankingRepository.findAll());

        List<RankingResponseDTO> rankingResponseDTOList = new ArrayList<>(ranking.size());
        for (Ranking r : ranking) {
            RankingResponseDTO rankingResponseDTO = devolverRanking(r, 2);
            rankingResponseDTOList.add(rankingResponseDTO);
        }

        return rankingResponseDTOList;
    }

    public List<RankingResponseDTO> rankingMapa3() {
        List<Ranking> ranking = new ArrayList<>();
        ranking.addAll(rankingRepository.findAll());

        List<RankingResponseDTO> rankingResponseDTOList = new ArrayList<>(ranking.size());
        for (Ranking r : ranking) {
            RankingResponseDTO rankingResponseDTO = devolverRanking(r, 3);
            rankingResponseDTOList.add(rankingResponseDTO);
        }

        return rankingResponseDTOList;
    }

    public List<RankingResponseDTO> rankingPVP() {
        List<Ranking> ranking = new ArrayList<>();
        ranking.addAll(rankingRepository.findAll());

        List<RankingResponseDTO> rankingResponseDTOList = new ArrayList<>(ranking.size());
        for (Ranking r : ranking) {
            if (r.getJogador().isBanido()){
                continue;
            }
            RankingResponseDTO rankingResponseDTO = devolverRanking(r, 4);
            rankingResponseDTOList.add(rankingResponseDTO);
        }

        return rankingResponseDTOList;
    }

    private RankingResponseDTO devolverRanking(Ranking ranking, int mapa) {
        RankingResponseDTO rankingResponseDTO = new RankingResponseDTO();
        rankingResponseDTO.setNome(ranking.getJogador().getNome());
        if (mapa == 1){
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_1());
        } else if (mapa == 2){
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_2());
        } else if (mapa == 3){
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_3());
        } else if (mapa == 4){
            rankingResponseDTO.setPontos(ranking.getPontos_pvp());
        } else {
            System.out.println("Algo está com erro");
        }

        return rankingResponseDTO;
    }
}
