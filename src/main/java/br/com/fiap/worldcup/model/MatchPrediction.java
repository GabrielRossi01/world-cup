package br.com.fiap.worldcup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String homeTeam;
    private String awayTeam;
    private String predictedScore;
    private String favoriteTeam;

    @Column(length = 3000)
    private String analysis;

    private Integer homeWinChance;
    private Integer drawChance;
    private Integer awayWinChance;
    private String confidence;
}
