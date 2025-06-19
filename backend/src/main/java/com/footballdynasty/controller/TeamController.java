package com.footballdynasty.controller;

import com.footballdynasty.dto.TeamDTO;
import com.footballdynasty.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
@Tag(name = "Teams", description = "Team management endpoints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieve all teams with optional search and pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved teams")
    public ResponseEntity<Page<TeamDTO>> getAllTeams(
            @Parameter(description = "Search term for filtering teams")
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<TeamDTO> teams = teamService.getAllTeams(search, pageable);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieve a specific team by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved team")
    @ApiResponse(responseCode = "404", description = "Team not found")
    public ResponseEntity<TeamDTO> getTeamById(
            @Parameter(description = "Team ID") @PathVariable UUID id) {
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/conference/{conference}")
    @Operation(summary = "Get teams by conference", description = "Retrieve all teams in a specific conference")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved teams")
    public ResponseEntity<List<TeamDTO>> getTeamsByConference(
            @Parameter(description = "Conference name") @PathVariable String conference) {
        List<TeamDTO> teams = teamService.getTeamsByConference(conference);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/conferences")
    @Operation(summary = "Get all conferences", description = "Retrieve all unique conference names")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved conferences")
    public ResponseEntity<List<String>> getAllConferences() {
        List<String> conferences = teamService.getAllConferences();
        return ResponseEntity.ok(conferences);
    }

    @GetMapping("/human")
    @Operation(summary = "Get human-controlled teams", description = "Retrieve all teams controlled by human players")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved human teams")
    public ResponseEntity<List<TeamDTO>> getHumanTeams() {
        List<TeamDTO> teams = teamService.getHumanTeams();
        return ResponseEntity.ok(teams);
    }

    @PostMapping
    @Operation(summary = "Create new team", description = "Create a new team")
    @ApiResponse(responseCode = "201", description = "Successfully created team")
    @ApiResponse(responseCode = "400", description = "Invalid team data")
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody TeamDTO teamDTO) {
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update team", description = "Update an existing team")
    @ApiResponse(responseCode = "200", description = "Successfully updated team")
    @ApiResponse(responseCode = "404", description = "Team not found")
    @ApiResponse(responseCode = "400", description = "Invalid team data")
    public ResponseEntity<TeamDTO> updateTeam(
            @Parameter(description = "Team ID") @PathVariable UUID id,
            @Valid @RequestBody TeamDTO teamDTO) {
        TeamDTO updatedTeam = teamService.updateTeam(id, teamDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete team", description = "Delete a team")
    @ApiResponse(responseCode = "204", description = "Successfully deleted team")
    @ApiResponse(responseCode = "404", description = "Team not found")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "Team ID") @PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}