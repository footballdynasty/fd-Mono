package com.footballdynasty.repository;

import com.footballdynasty.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.selectedTeam WHERE u.id = :id")
    Optional<User> findByIdWithSelectedTeam(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.selectedTeam WHERE u.username = :username")
    Optional<User> findByUsernameWithSelectedTeam(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Iterable<User> findAllActiveUsers();
}