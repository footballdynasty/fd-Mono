package com.footballdynasty.repository;

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
public interface TeamRepository extends JpaRepository<Team, UUID> {
    
    Optional<Team> findByName(String name);
    
    List<Team> findByConferenceOrderByName(String conference);
    
    List<Team> findByIsHumanTrue();
    
    @Query("SELECT DISTINCT t.conference FROM Team t ORDER BY t.conference")
    List<String> findAllConferences();
    
    @Query("SELECT t FROM Team t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.coach) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.conference) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Team> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT t FROM Team t JOIN t.standings s WHERE s.year = :year ORDER BY s.wins DESC, s.losses ASC")
    List<Team> findByYearOrderByStandings(@Param("year") Integer year);
}