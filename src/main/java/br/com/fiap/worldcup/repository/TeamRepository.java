package br.com.fiap.worldcup.repository;

import br.com.fiap.worldcup.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Team> findByNameContainingIgnoreCase(String name);
}
