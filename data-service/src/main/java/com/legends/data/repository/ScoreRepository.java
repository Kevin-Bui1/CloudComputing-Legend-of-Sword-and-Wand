package com.legends.data.repository;

import com.legends.data.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT MAX(s.score) FROM Score s WHERE s.userId = :userId")
    Optional<Integer> findBestScoreByUserId(Long userId);

    @Query("SELECT s FROM Score s ORDER BY s.score DESC")
    List<Score> findTopScores(org.springframework.data.domain.Pageable pageable);
}
