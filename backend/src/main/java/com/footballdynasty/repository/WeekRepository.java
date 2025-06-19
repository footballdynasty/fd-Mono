package com.footballdynasty.repository;

import com.footballdynasty.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeekRepository extends JpaRepository<Week, UUID> {
    
    Optional<Week> findByYearAndWeekNumber(Integer year, Integer weekNumber);
    
    List<Week> findByYearOrderByWeekNumber(Integer year);
    
    @Query("SELECT DISTINCT w.year FROM Week w ORDER BY w.year DESC")
    List<Integer> findDistinctYears();
    
    @Query("SELECT w FROM Week w WHERE w.year = :year AND w.weekNumber BETWEEN :startWeek AND :endWeek ORDER BY w.weekNumber")
    List<Week> findByYearAndWeekRange(@Param("year") Integer year, @Param("startWeek") Integer startWeek, @Param("endWeek") Integer endWeek);
}