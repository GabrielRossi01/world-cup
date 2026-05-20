package br.com.fiap.worldcup.repository;

import br.com.fiap.worldcup.model.MatchPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {
    List<MatchPrediction> findByHomeTeamIgnoreCaseAndAwayTeamIgnoreCase(String homeTeam, String awayTeam);

    List<MatchPrediction> findByFavoriteTeamIgnoreCase(String favoriteTeam);

    List<MatchPrediction> findByHomeTeamIgnoreCaseOrAwayTeamIgnoreCase(String homeTeam, String awayTeam);
}
