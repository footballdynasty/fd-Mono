package com.footballdynasty.mapper;

import com.footballdynasty.dto.GameDTO;
import com.footballdynasty.entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface GameMapper {
    
    @Mapping(target = "homeTeamId", source = "homeTeam.id")
    @Mapping(target = "homeTeamName", source = "homeTeam.name")
    @Mapping(target = "homeTeamImageUrl", source = "homeTeam.imageUrl")
    @Mapping(target = "awayTeamId", source = "awayTeam.id")
    @Mapping(target = "awayTeamName", source = "awayTeam.name")
    @Mapping(target = "awayTeamImageUrl", source = "awayTeam.imageUrl")
    @Mapping(target = "weekId", source = "week.id")
    @Mapping(target = "weekNumber", source = "week.weekNumber")
    @Mapping(target = "year", source = "week.year")
    @Mapping(target = "statusDisplay", source = "status", qualifiedByName = "statusToDisplay")
    @Mapping(target = "scoreDisplay", source = ".", qualifiedByName = "generateScoreDisplay")
    @Mapping(target = "isCompleted", source = "status", qualifiedByName = "statusToCompleted")
    @Mapping(target = "winnerName", source = ".", qualifiedByName = "getWinnerName")
    GameDTO toDTO(Game game);
    
    @Named("statusToDisplay")
    default String statusToDisplay(Game.GameStatus status) {
        if (status == null) return "Unknown";
        
        switch (status) {
            case SCHEDULED:
                return "Scheduled";
            case IN_PROGRESS:
                return "In Progress";
            case COMPLETED:
                return "Final";
            case CANCELLED:
                return "Cancelled";
            default:
                return status.toString();
        }
    }
    
    @Named("generateScoreDisplay")
    default String generateScoreDisplay(Game game) {
        if (game.getStatus() == Game.GameStatus.SCHEDULED) {
            return "TBD";
        } else if (game.getStatus() == Game.GameStatus.CANCELLED) {
            return "Cancelled";
        } else if (game.getHomeScore() != null && game.getAwayScore() != null) {
            return game.getHomeScore() + " - " + game.getAwayScore();
        } else {
            return "0 - 0";
        }
    }
    
    @Named("statusToCompleted")
    default Boolean statusToCompleted(Game.GameStatus status) {
        return status == Game.GameStatus.COMPLETED;
    }
    
    @Named("getWinnerName")
    default String getWinnerName(Game game) {
        if (game.getStatus() != Game.GameStatus.COMPLETED || 
            game.getHomeScore() == null || game.getAwayScore() == null) {
            return null;
        }
        
        if (game.getHomeScore() > game.getAwayScore()) {
            return game.getHomeTeam() != null ? game.getHomeTeam().getName() : null;
        } else if (game.getAwayScore() > game.getHomeScore()) {
            return game.getAwayTeam() != null ? game.getAwayTeam().getName() : null;
        } else {
            return "Tie"; // Though unlikely in CFB
        }
    }
}