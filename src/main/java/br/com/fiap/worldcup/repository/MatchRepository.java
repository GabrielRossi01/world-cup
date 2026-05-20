package br.com.fiap.worldcup.repository;

import br.com.fiap.worldcup.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        select m
        from Match m
        where lower(m.homeTeam.name) = lower(:teamName)
           or lower(m.awayTeam.name) = lower(:teamName)
        order by m.matchDate asc
    """)
    List<Match> findByTeam(String teamName);

    List<Match> findByGroupNameIgnoreCaseOrderByMatchDateAsc(String groupName);

    List<Match> findByStageIgnoreCaseOrderByMatchDateAsc(String stage);

    List<Match> findByMatchDateAfterOrderByMatchDateAsc(LocalDateTime dateTime);

    @Query("""
        select m
        from Match m
        where (lower(m.homeTeam.name) = lower(:teamName)
           or lower(m.awayTeam.name) = lower(:teamName))
          and m.matchDate > :dateTime
        order by m.matchDate asc
    """)
    List<Match> findUpcomingMatchesByTeam(String teamName, LocalDateTime dateTime);
}
