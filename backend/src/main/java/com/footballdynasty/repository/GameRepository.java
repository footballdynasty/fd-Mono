package com.footballdynasty.repository;

import com.footballdynasty.entity.Game;
import com.footballdynasty.entity.Team;
import com.footballdynasty.entity.Week;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {
    
    List<Game> findByWeekOrderByDate(Week week);
    
    List<Game> findByHomeTeamOrAwayTeamOrderByDate(Team homeTeam, Team awayTeam);
    
    List<Game> findByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT g FROM Game g WHERE g.week.year = :year ORDER BY g.week.weekNumber, g.date")
    List<Game> findByYearOrderByWeekAndDate(@Param("year") Integer year);
    
    @Query("SELECT g FROM Game g WHERE " +
           "(g.homeTeam = :team OR g.awayTeam = :team) " +
           "AND g.week.year = :year " +
           "ORDER BY g.week.weekNumber, g.date")
    List<Game> findByTeamAndYearOrderByWeekAndDate(@Param("team") Team team, @Param("year") Integer year);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'COMPLETED' " +
           "AND (g.homeTeam = :team OR g.awayTeam = :team) " +
           "ORDER BY g.date DESC")
    Page<Game> findCompletedGamesByTeam(@Param("team") Team team, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.date >= CURRENT_DATE " +
           "AND (g.homeTeam = :team OR g.awayTeam = :team) " +
           "ORDER BY g.date ASC")
    List<Game> findUpcomingGamesByTeam(@Param("team") Team team);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'COMPLETED' " +
           "AND (g.homeTeam.id IN :teamIds OR g.awayTeam.id IN :teamIds) " +
           "AND g.date BETWEEN :startDate AND :endDate " +
           "ORDER BY g.date")
    List<Game> findCompletedGamesByTeamsAndDateRange(@Param("teamIds") List<UUID> teamIds, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    // Additional methods for GameController
    @Query("SELECT g FROM Game g WHERE g.week.year = :year ORDER BY g.date DESC")
    Page<Game> findByYearOrderByDateDesc(@Param("year") Integer year, Pageable pageable);
    
    @Query("SELECT g FROM Game g ORDER BY g.date DESC")
    Page<Game> findAllOrderByDateDesc(Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE (g.homeTeam.id = :teamId OR g.awayTeam.id = :teamId) " +
           "AND g.week.year = :year ORDER BY g.date DESC")
    List<Game> findByTeamAndYearOrderByDateDesc(@Param("teamId") UUID teamId, @Param("year") Integer year);
    
    @Query("SELECT g FROM Game g WHERE g.date >= :currentDate " +
           "AND (g.homeTeam.id = :teamId OR g.awayTeam.id = :teamId) " +
           "ORDER BY g.date ASC")
    List<Game> findUpcomingGamesByTeam(@Param("teamId") UUID teamId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT g FROM Game g WHERE g.date >= :currentDate ORDER BY g.date ASC")
    List<Game> findUpcomingGames(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'COMPLETED' " +
           "AND g.date < :currentDate " +
           "AND (g.homeTeam.id = :teamId OR g.awayTeam.id = :teamId) " +
           "ORDER BY g.date DESC")
    List<Game> findRecentCompletedGamesByTeam(@Param("teamId") UUID teamId, 
                                            @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'COMPLETED' " +
           "AND g.date < :currentDate " +
           "ORDER BY g.date DESC")
    List<Game> findRecentCompletedGames(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT g FROM Game g WHERE g.week.id = :weekId ORDER BY g.date")
    List<Game> findByWeekIdOrderByDate(@Param("weekId") UUID weekId);
}