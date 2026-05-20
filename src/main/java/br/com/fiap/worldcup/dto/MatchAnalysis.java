package br.com.fiap.worldcup.dto;

import java.util.List;

public record MatchAnalysis (
     String match,
     String favorite,
     Integer homeWinChance,
     Integer drawChance,
     Integer awayWinChance,
     List<String> keyPoints
) {}
