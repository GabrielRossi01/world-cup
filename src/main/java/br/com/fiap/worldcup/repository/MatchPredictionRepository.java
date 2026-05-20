package br.com.fiap.worldcup.repository;

import br.com.fiap.worldcup.model.MatchPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPredictionRepository extends JpaRepository<MatchPrediction, Long> {
}
