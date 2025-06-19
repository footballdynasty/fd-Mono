package com.footballdynasty.repository;

import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StandingRepository extends JpaRepository<Standing, UUID> {
    
    List<Standing> findByYearOrderByWinsDescLossesAsc(Integer year);
    
    Optional<Standing> findByTeamAndYear(Team team, Integer year);
    
    List<Standing> findByTeamOrderByYearDesc(Team team);
    
    @Query("SELECT s FROM Standing s WHERE s.year = :year AND s.rank IS NOT NULL ORDER BY s.rank ASC")
    List<Standing> findRankedTeamsByYear(@Param("year") Integer year);
    
    @Query("SELECT s FROM Standing s WHERE s.year = :year AND s.receivingVotes > 0 ORDER BY s.receivingVotes DESC")
    List<Standing> findTeamsReceivingVotesByYear(@Param("year") Integer year);
    
    @Query("SELECT s FROM Standing s WHERE s.team.conference = :conference AND s.year = :year " +
           "ORDER BY s.wins DESC, s.losses ASC")
    List<Standing> findByConferenceAndYearOrderByRecord(@Param("conference") String conference, @Param("year") Integer year);
    
    @Query("SELECT s FROM Standing s WHERE s.team.id IN :teamIds AND s.year = :year " +
           "ORDER BY s.conferenceRank ASC")
    List<Standing> findByTeamsAndYearOrderByConferenceRank(@Param("teamIds") List<UUID> teamIds, @Param("year") Integer year);
    
    // Additional methods for pagination support
    Page<Standing> findByYearOrderByWinsDescLossesAsc(Integer year, Pageable pageable);
    
    Page<Standing> findByTeamOrderByYearDesc(Team team, Pageable pageable);
    
    @Query("SELECT s FROM Standing s WHERE s.team.conference = :conference AND s.year = :year " +
           "ORDER BY s.wins DESC, s.losses ASC")
    Page<Standing> findByConferenceAndYearOrderByRecord(@Param("conference") String conference, @Param("year") Integer year, Pageable pageable);
    
    @Query("SELECT s FROM Standing s WHERE s.year = :year AND s.rank IS NOT NULL ORDER BY s.rank ASC")
    Page<Standing> findRankedTeamsByYear(@Param("year") Integer year, Pageable pageable);
}