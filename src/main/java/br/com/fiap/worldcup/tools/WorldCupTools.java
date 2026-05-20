package br.com.fiap.worldcup.tools;

import br.com.fiap.worldcup.model.MatchPrediction;
import br.com.fiap.worldcup.model.Team;
import br.com.fiap.worldcup.repository.MatchPredictionRepository;
import br.com.fiap.worldcup.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorldCupTools {

    private final TeamRepository teamRepository;
    private final MatchPredictionRepository matchPredictionRepository;

    public WorldCupTools(TeamRepository teamRepository, MatchPredictionRepository matchPredictionRepository) {
        this.teamRepository = teamRepository;
        this.matchPredictionRepository = matchPredictionRepository;
    }

    @Tool(name = "find_team_by_name", description = "Busca informações de uma seleção da Copa do Mundo 2026 pelo nome")
    public Team findTeamByName (
            @ToolParam(description = "Nome da seleção que deseja consultar") String teamName
    ) {
        log.info("Searching team by name: {}", teamName);

        return teamRepository.findByNameIgnoreCase(teamName)
                .orElse(null);
    }

    @Tool(name = "save_match_prediction", description = "Salva uma previsão de partida da Copa do Mundo 2026")
    public void saveMatchPrediction(
            @ToolParam(description = "Dados completos da previsão da partida")MatchPrediction prediction
    ) {
        log.info("Saving match prediction: {} x {}", prediction.getHomeTeam(), prediction.getAwayTeam());
        prediction.setId(null);
        matchPredictionRepository.save(prediction);
    }

    @Tool(name = "list_all_teams", description = "Lista todas as seleções cadastradas no sistema")
    public Iterable<Team> listAllTeams() {
        log.info("Listing all teams");
        return teamRepository.findAll();
    }
}
