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
        testTeam.setConference("ACC");
        testTeam.setCoach("Test Coach");
        testTeam.setImageUrl("http://example.com/logo.png");
        testTeam = teamRepository.save(testTeam);

        // Create test standing
        testStanding = new Standing();
        testStanding.setTeam(testTeam);
        testStanding.setYear(2024);
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
        mockMvc.perform(get("/api/v2/standings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testStanding.getId().toString())))
                .andExpect(jsonPath("$.content[0].year", is(2024)))
                .andExpect(jsonPath("$.content[0].wins", is(8)))
                .andExpect(jsonPath("$.content[0].losses", is(3)));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithYearFilter() throws Exception {
        mockMvc.perform(get("/api/v2/standings")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].year", is(2024)));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithConferenceFilter() throws Exception {
        mockMvc.perform(get("/api/v2/standings")
                        .param("conference", "ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].team.conference", is("ACC")));
    }

    @Test
    @WithMockUser
    void testGetAllStandingsWithYearAndConferenceFilter() throws Exception {
        mockMvc.perform(get("/api/v2/standings")
                        .param("year", "2024")
                        .param("conference", "ACC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser
    void testGetStandingById() throws Exception {
        mockMvc.perform(get("/api/v2/standings/{id}", testStanding.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testStanding.getId().toString())))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.wins", is(8)))
                .andExpect(jsonPath("$.losses", is(3)))
                .andExpect(jsonPath("$.team.name", is("Test Team")));
    }

    @Test
    @WithMockUser
    void testGetStandingByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v2/standings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetStandingsByTeam() throws Exception {
        mockMvc.perform(get("/api/v2/standings/team/{teamId}", testTeam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].team.id", is(testTeam.getId().toString())));
    }

    @Test
    @WithMockUser
    void testGetStandingsByTeamNotFound() throws Exception {
        UUID nonExistentTeamId = UUID.randomUUID();
        mockMvc.perform(get("/api/v2/standings/team/{teamId}", nonExistentTeamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser
    void testGetStandingByTeamAndYear() throws Exception {
        mockMvc.perform(get("/api/v2/standings/team/{teamId}/year/{year}", testTeam.getId(), 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team.id", is(testTeam.getId().toString())))
                .andExpect(jsonPath("$.year", is(2024)));
    }

    @Test
    @WithMockUser
    void testGetStandingByTeamAndYearNotFound() throws Exception {
        mockMvc.perform(get("/api/v2/standings/team/{teamId}/year/{year}", testTeam.getId(), 2023))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetConferenceStandings() throws Exception {
        mockMvc.perform(get("/api/v2/standings/conference/{conference}/year/{year}", "ACC", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].team.conference", is("ACC")))
                .andExpect(jsonPath("$[0].year", is(2024)));
    }

    @Test
    @WithMockUser
    void testGetTopRankedTeams() throws Exception {
        mockMvc.perform(get("/api/v2/standings/ranked/year/{year}", 2024)
                        .param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rank", is(15)));
    }

    @Test
    @WithMockUser
    void testGetTeamsReceivingVotes() throws Exception {
        mockMvc.perform(get("/api/v2/standings/votes/year/{year}", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].votes", is(25)));
    }

    @Test
    @WithMockUser
    void testCreateStanding() throws Exception {
        // Create another team for the new standing
        Team newTeam = new Team();
        newTeam.setName("New Team");
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

        mockMvc.perform(post("/api/v2/standings")
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

        mockMvc.perform(post("/api/v2/standings")
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

        mockMvc.perform(put("/api/v2/standings/{id}", testStanding.getId())
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

        mockMvc.perform(put("/api/v2/standings/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteStanding() throws Exception {
        mockMvc.perform(delete("/api/v2/standings/{id}", testStanding.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/v2/standings/{id}", testStanding.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteStandingNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v2/standings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCalculateStandings() throws Exception {
        mockMvc.perform(post("/api/v2/standings/calculate/{year}", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @WithMockUser
    void testCalculateConferenceStandings() throws Exception {
        mockMvc.perform(post("/api/v2/standings/calculate/conference/{conference}/year/{year}", "ACC", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.conference", is("ACC")))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @WithMockUser
    void testPaginationFunctionality() throws Exception {
        // Create additional standings for pagination test
        for (int i = 0; i < 15; i++) {
            Team team = new Team();
            team.setName("Team " + i);
            team.setConference("Big 12");
            team.setCoach("Coach " + i);
            team.setImageUrl("http://example.com/logo" + i + ".png");
            team = teamRepository.save(team);

            Standing standing = new Standing();
            standing.setTeam(team);
            standing.setYear(2024);
            standing.setWins(10 - i);
            standing.setLosses(i);
            standing.setConferenceWins(8 - i);
            standing.setConferenceLosses(i);
            standing.setRank(i + 1);
            standing.setConferenceRank(i + 1);
            standing.setReceivingVotes(100 - (i * 5));
            standingRepository.save(standing);
        }

        // Test first page
        mockMvc.perform(get("/api/v2/standings")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(16))) // 15 + original test standing
                .andExpect(jsonPath("$.totalPages", is(4)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));

        // Test second page
        mockMvc.perform(get("/api/v2/standings")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(false)));
    }
}