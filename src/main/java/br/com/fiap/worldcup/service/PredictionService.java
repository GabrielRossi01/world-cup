package br.com.fiap.worldcup.service;

import br.com.fiap.worldcup.dto.MatchAnalysis;
import br.com.fiap.worldcup.model.MatchPrediction;
import br.com.fiap.worldcup.repository.MatchPredictionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    private final ChatClient chatClient;
    private final MatchPredictionRepository matchPredictionRepository;

    public PredictionService(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             MatchPredictionRepository matchPredictionRepository) {
        this.matchPredictionRepository = matchPredictionRepository;
        this.chatClient = builder
                .defaultSystem("""
                        Você é um analista especialista na Copa do Mundo 2026.
                        Gere previsões objetivas e estruturadas.
                        Responda sempre em formato compatível com a estrutura solicitada.
                        Não invente probabilidades absurdas e mantenha coerência entre os percentuais.
                        """)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    public MatchPrediction generateAndSavePrediction(String homeTeam, String awayTeam) {
        MatchAnalysis analysis = chatClient.prompt()
                .user("""
                        Analise a partida entre %s e %s.
                        Informe:
                        - nome do confronto
                        - favorito
                        - chance de vitória do mandante
                        - chance de empate
                        - chance de vitória do visitante
                        - lista de pontos-chave táticos
                        """.formatted(homeTeam, awayTeam))
                .call()
                .entity(MatchAnalysis.class);

        MatchPrediction entity = new MatchPrediction();
        entity.setHomeTeam(homeTeam);
        entity.setAwayTeam(awayTeam);
        entity.setFavoriteTeam(analysis.favorite());
        entity.setHomeWinChance(analysis.homeWinChance());
        entity.setDrawChance(analysis.drawChance());
        entity.setAwayWinChance(analysis.awayWinChance());
        entity.setPredictedScore(buildPredictedScore(analysis));
        entity.setAnalysis(String.join(" | ", analysis.keyPoints()));
        entity.setConfidence(calculateConfidence(analysis));

        return matchPredictionRepository.save(entity);
    }

    private String buildPredictedScore(MatchAnalysis analysis) {
        if (analysis.homeWinChance() > analysis.awayWinChance()) {
            return "2 x 1";
        }
        if (analysis.awayWinChance() > analysis.homeWinChance()) {
            return "1 x 2";
        }
        return "1 x 1";
    }

    private String calculateConfidence(MatchAnalysis analysis) {
        int max = Math.max(
                analysis.homeWinChance(),
                Math.max(analysis.drawChance(), analysis.awayWinChance())
        );

        if (max >= 70) return "Alta";
        if (max >= 50) return "Média";
        return "Baixa";
    }
}