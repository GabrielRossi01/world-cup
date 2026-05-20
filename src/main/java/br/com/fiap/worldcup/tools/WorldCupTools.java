package br.com.fiap.worldcup.tools;

import br.com.fiap.worldcup.model.Match;
import br.com.fiap.worldcup.repository.MatchRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class WorldCupTools {

    private final MatchRepository matchRepository;

    public WorldCupTools(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Tool(description = "Retorna a estreia de uma seleção na Copa do Mundo 2026, com data, adversário e estádio.")
    public String getTeamDebut(String teamName) {
        List<Match> matches = matchRepository.findByTeam(teamName);

        return matches.stream()
                .filter(m -> m.getMatchDate() != null)
                .min(Comparator.comparing(Match::getMatchDate))
                .map(m -> {
                    String opponent = m.getHomeTeam().getName().equalsIgnoreCase(teamName)
                            ? m.getAwayTeam().getName()
                            : m.getHomeTeam().getName();

                    return "%s estreia contra %s em %s, no estádio %s."
                            .formatted(
                                    teamName,
                                    opponent,
                                    m.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR"))),
                                    m.getStadium()
                            );
                })
                .orElse("Não encontrei a estreia da seleção " + teamName + " na base cadastrada.");
    }

    @Tool(description = "Retorna a agenda completa de jogos de uma seleção na Copa do Mundo 2026.")
    public String getTeamSchedule(String teamName) {
        List<Match> matches = matchRepository.findByTeam(teamName);

        if (matches.isEmpty()) {
            return "Não encontrei jogos da seleção " + teamName + " na base cadastrada.";
        }

        return matches.stream()
                .filter(m -> m.getMatchDate() != null)
                .sorted(Comparator.comparing(Match::getMatchDate))
                .map(m -> "%s x %s - %s - %s"
                        .formatted(
                                m.getHomeTeam().getName(),
                                m.getAwayTeam().getName(),
                                m.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"))),
                                m.getStadium()
                        ))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Retorna os próximos jogos de uma seleção a partir da data atual.")
    public String getUpcomingMatches(String teamName) {
        List<Match> matches = matchRepository.findUpcomingMatchesByTeam(teamName, LocalDateTime.now());

        if (matches.isEmpty()) {
            return "Não encontrei próximos jogos da seleção " + teamName + " na base cadastrada.";
        }

        return matches.stream()
                .map(m -> "%s x %s - %s - %s"
                        .formatted(
                                m.getHomeTeam().getName(),
                                m.getAwayTeam().getName(),
                                m.getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"))),
                                m.getStadium()
                        ))
                .collect(Collectors.joining("\n"));
    }
}