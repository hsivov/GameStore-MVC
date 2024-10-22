package org.example.gamestoreapp.repository;

import org.example.gamestoreapp.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByIdIn(Set<Long> gameIds);
}
