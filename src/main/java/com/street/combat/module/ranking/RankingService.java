package com.street.combat.module.ranking;

import com.street.combat.module.ranking.dto.request.RankingRequestDTO;
import com.street.combat.module.ranking.dto.response.RankingResponseDTO;
import com.street.combat.module.usuario.Usuario;
import com.street.combat.module.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(
            RankingRepository rankingRepository
    )
    {
        this.rankingRepository = rankingRepository;
    }

    public void adicionarNoRanking(Usuario usuario){
        if (rankingRepository.existsByJogadorId(usuario.getId())) {
            return;
        }
        Ranking ranking = new Ranking();
        ranking.setJogador(usuario);
        ranking.setPontos_mapa_1(0);
        ranking.setPontos_mapa_2(0);
        ranking.setPontos_mapa_3(0);
        rankingRepository.save(ranking);
    }

    public void atualizarRanking(RankingRequestDTO rankingRequestDTO) {
        Ranking ranking = rankingRepository.findByJogadorId(rankingRequestDTO.id())
                    .orElseThrow(() -> new RuntimeException("Ranking não encontrado"));

        if (rankingRequestDTO.mapa() == 1) {
            ranking.setPontos_mapa_1(rankingRequestDTO.pontos());
        } else if (rankingRequestDTO.mapa() == 2) {
            ranking.setPontos_mapa_2(rankingRequestDTO.pontos());
        } else {
            ranking.setPontos_mapa_3(rankingRequestDTO.pontos());
        }

        rankingRepository.save(ranking);
    }

    public List<RankingResponseDTO> rankingMapa1() {
        List<Ranking> ranking = new ArrayList<>();
        ranking.addAll(rankingRepository.findAll());

        List<RankingResponseDTO> rankingResponseDTOList = new ArrayList<>(ranking.size());
        for (Ranking r : ranking) {
            RankingResponseDTO rankingResponseDTO = devolverRanking(r, 1);
            rankingResponseDTOList.add(rankingResponseDTO);
        }
        System.out.println(ranking);
        System.out.println(rankingResponseDTOList);
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

    private RankingResponseDTO devolverRanking(Ranking ranking, int mapa) {
        RankingResponseDTO rankingResponseDTO = new RankingResponseDTO();
        rankingResponseDTO.setNome(ranking.getJogador().getNome());
        if (mapa == 1){
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_1());
        } else if (mapa == 2){
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_2());
        } else if (mapa == 3) {
            rankingResponseDTO.setPontos(ranking.getPontos_mapa_3());
        } else {
            System.out.println("Algo está com erro");
        }

        return rankingResponseDTO;
    }
}
