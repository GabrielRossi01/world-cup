package br.com.fiap.worldcup.tools;

import br.com.fiap.worldcup.model.Match;
import br.com.fiap.worldcup.repository.MatchRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class WorldCupTools {

    private final MatchRepository matchRepository;

    public WorldCupTools(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Tool(description = "Retorna a estreia de uma seleção na Copa do Mundo 2026. Use quando o usuário perguntar quando um time estreia.")
    public String getTeamDebut(
            @ToolParam(description = "Nome da seleção, por exemplo: Brasil, Argentina, França")
            String teamName
    ) {
        var matches = matchRepository.findByTeam(teamName);

        return matches.stream()
                .filter(m -> m.getMatchDate() != null)
                .min(Comparator.comparing(Match::getMatchDate))
                .map(m -> {
                    String opponent = m.getHomeTeam().getName().equalsIgnoreCase(teamName)
                            ? m.getAwayTeam().getName()
                            : m.getHomeTeam().getName();

                    return "%s estreia contra %s em %s no estádio %s."
                            .formatted(
                                    teamName,
                                    opponent,
                                    m.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                    m.getStadium()
                            );
                })
                .orElse("Não encontrei a estreia da seleção " + teamName + ".");
    }

    @Tool(description = "Retorna a agenda completa de jogos de uma seleção da Copa do Mundo 2026. Use quando o usuário pedir jogos, calendário ou agenda de um time.")
    public String getTeamSchedule(
            @ToolParam(description = "Nome da seleção, por exemplo: Brasil, Alemanha, Inglaterra")
            String teamName
    ) {
        var matches = matchRepository.findByTeam(teamName);

        if (matches.isEmpty()) {
            return "Não encontrei jogos da seleção " + teamName + ".";
        }

        return matches.stream()
                .filter(m -> m.getMatchDate() != null)
                .sorted(Comparator.comparing(Match::getMatchDate))
                .map(m -> "%s x %s - %s - %s"
                        .formatted(
                                m.getHomeTeam().getName(),
                                m.getAwayTeam().getName(),
                                m.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                m.getStadium()
                        ))
                .collect(Collectors.joining("\n"));
    }
}
