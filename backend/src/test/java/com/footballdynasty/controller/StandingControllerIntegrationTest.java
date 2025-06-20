package com.footballdynasty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import com.footballdynasty.repository.StandingRepository;
import com.footballdynasty.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StandingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StandingRepository standingRepository;

    @Autowired
    private TeamRepository teamRepository;

    private Team testTeam;
    private Standing testStanding;

    @BeforeEach
    void setUp() {
        standingRepository.deleteAll();
        teamRepository.deleteAll();

        // Create test team
        testTeam = new Team();
        testTeam.setName("Test Team");
        testTeam.setUsername("test-team-user");
        testTeam.setConference("ACC");
        testTeam.setCoach("Test Coach");
        testTeam.setImageUrl("http://example.com/logo.png");
        testTeam = teamRepository.save(testTeam);

        // Create test standing for current year with rank
        int currentYear = java.time.LocalDateTime.now().getYear();
        testStanding = new Standing();
        testStanding.setTeam(testTeam);
        testStanding.setYear(currentYear);
        testStanding.setWins(8);
        testStanding.setLosses(3);
        testStanding.setConferenceWins(5);
        testStanding.setConferenceLosses(2);
        // Note: Points for/against not in Standing entity
        testStanding.setRank(15);
        testStanding.setConferenceRank(3);
        testStanding.setReceivingVotes(25);
        testStanding = standingRepository.save(testStanding);
    }

    @Test
    @WithMockUser
    void testGetAllStandings() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings"))
                .andDo(result -> {
                    System.out.println("Response status: " + result.getResponse().getStatus());
                    System.out.println("Response body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testStanding.getId().toString())))
                .andExpect(jsonPath("$.content[0].year", is(currentYear)))
                .andExpect(jsonPath("$.content[0].wins", is(8)))
                .andExpect(jsonPath("$.content[0].losses", is(3)));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithYearFilter() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings")
                        .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].year", is(currentYear)));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithConferenceFilter() throws Exception {
        mockMvc.perform(get("/standings")
                        .param("conference", "ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].team.conference", is("ACC")));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithYearAndConferenceFilter() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings")
                        .param("year", String.valueOf(currentYear))
                        .param("conference", "ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser
    void testGetStandingById() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings/{id}", testStanding.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testStanding.getId().toString())))
                .andExpect(jsonPath("$.year", is(currentYear)))
                .andExpect(jsonPath("$.wins", is(8)))
                .andExpect(jsonPath("$.losses", is(3)))
                .andExpect(jsonPath("$.team.name", is("Test Team")));
    }

    @Test
    @WithMockUser
    void testGetStandingByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/standings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetStandingsByTeam() throws Exception {
        mockMvc.perform(get("/standings/team/{teamId}", testTeam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].team.id", is(testTeam.getId().toString())));
    }

    @Test
    @WithMockUser
    void testGetStandingsByTeamNotFound() throws Exception {
        UUID nonExistentTeamId = UUID.randomUUID();
        mockMvc.perform(get("/standings/team/{teamId}", nonExistentTeamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser
    void testGetStandingByTeamAndYear() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings/team/{teamId}/year/{year}", testTeam.getId(), currentYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team.id", is(testTeam.getId().toString())))
                .andExpect(jsonPath("$.year", is(currentYear)));
    }

    @Test
    @WithMockUser
    void testGetStandingByTeamAndYearNotFound() throws Exception {
        mockMvc.perform(get("/standings/team/{teamId}/year/{year}", testTeam.getId(), 2023))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetConferenceStandings() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings/conference/{conference}/year/{year}", "ACC", currentYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].team.conference", is("ACC")))
                .andExpect(jsonPath("$[0].year", is(currentYear)));
    }

    @Test
    @WithMockUser
    void testGetTopRankedTeams() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings/ranked/year/{year}", currentYear)
                        .param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rank", is(15)));
    }

    @Test
    @WithMockUser
    void testGetTeamsReceivingVotes() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        mockMvc.perform(get("/standings/votes/year/{year}", currentYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].receiving_votes", is(25)));
    }

    @Test
    @WithMockUser
    void testCreateStanding() throws Exception {
        // Create another team for the new standing
        Team newTeam = new Team();
        newTeam.setName("New Team");
        newTeam.setUsername("new-team-user");
        newTeam.setConference("SEC");
        newTeam.setCoach("New Coach");
        newTeam.setImageUrl("http://example.com/newlogo.png");
        newTeam = teamRepository.save(newTeam);

        StandingCreateDTO createDTO = new StandingCreateDTO();
        createDTO.setTeamId(newTeam.getId());
        createDTO.setYear(2024);
        createDTO.setWins(10);
        createDTO.setLosses(2);
        createDTO.setConferenceWins(7);
        createDTO.setConferenceLosses(1);
        // Note: Points for/against not in DTO
        createDTO.setRank(5);
        createDTO.setConferenceRank(1);
        createDTO.setReceivingVotes(150);

        mockMvc.perform(post("/standings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.wins", is(10)))
                .andExpect(jsonPath("$.losses", is(2)))
                .andExpect(jsonPath("$.team.id", is(newTeam.getId().toString())));
    }

    @Test
    @WithMockUser
    void testCreateStandingInvalidData() throws Exception {
        StandingCreateDTO invalidDTO = new StandingCreateDTO();
        // Missing required fields

        mockMvc.perform(post("/standings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateStanding() throws Exception {
        StandingUpdateDTO updateDTO = new StandingUpdateDTO();
        updateDTO.setWins(9);
        updateDTO.setLosses(3);
        updateDTO.setConferenceWins(6);
        updateDTO.setConferenceLosses(2);
        // Note: Points for/against not in DTO
        updateDTO.setRank(12);
        updateDTO.setConferenceRank(2);
        updateDTO.setReceivingVotes(50);

        mockMvc.perform(put("/standings/{id}", testStanding.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wins", is(9)))
                .andExpect(jsonPath("$.losses", is(3)))
                .andExpect(jsonPath("$.rank", is(12)));
    }

    @Test
    @WithMockUser
    void testUpdateStandingNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        StandingUpdateDTO updateDTO = new StandingUpdateDTO();
        updateDTO.setWins(9);

        mockMvc.perform(put("/standings/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteStanding() throws Exception {
        mockMvc.perform(delete("/standings/{id}", testStanding.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/standings/{id}", testStanding.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteStandingNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/standings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCalculateStandings() throws Exception {
        mockMvc.perform(post("/standings/calculate/{year}", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @WithMockUser
    void testCalculateConferenceStandings() throws Exception {
        mockMvc.perform(post("/standings/calculate/conference/{conference}/year/{year}", "ACC", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.conference", is("ACC")))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @WithMockUser
    void testPaginationFunctionality() throws Exception {
        int currentYear = java.time.LocalDateTime.now().getYear();
        // Create additional standings for pagination test
        for (int i = 0; i < 15; i++) {
            Team team = new Team();
            team.setName("Team " + i);
            team.setUsername("team-" + i + "-user");
            team.setConference("Big 12");
            team.setCoach("Coach " + i);
            team.setImageUrl("http://example.com/logo" + i + ".png");
            team = teamRepository.save(team);

            Standing standing = new Standing();
            standing.setTeam(team);
            standing.setYear(currentYear);
            standing.setWins(Math.max(0, 10 - i));  // Ensure non-negative wins
            standing.setLosses(i);
            standing.setConferenceWins(Math.max(0, 8 - i));  // Ensure non-negative conference wins
            standing.setConferenceLosses(i);
            standing.setRank(i + 1);
            standing.setConferenceRank(i + 1);
            standing.setReceivingVotes(Math.max(0, 100 - (i * 5)));  // Ensure non-negative votes
            standingRepository.save(standing);
        }

        // Test first page
        mockMvc.perform(get("/standings")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(16))) // 15 + original test standing
                .andExpect(jsonPath("$.totalPages", is(4)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));

        // Test second page
        mockMvc.perform(get("/standings")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(false)));
    }
}