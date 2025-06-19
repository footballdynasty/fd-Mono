package com.footballdynasty.mapper;

import com.footballdynasty.dto.GameDTO;
import com.footballdynasty.entity.Game;
import com.footballdynasty.entity.Team;
import com.footballdynasty.entity.Week;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-18T20:15:33-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Homebrew)"
)
@Component
public class GameMapperImpl implements GameMapper {

    @Override
    public GameDTO toDTO(Game game) {
        if ( game == null ) {
            return null;
        }

        GameDTO gameDTO = new GameDTO();

        UUID id = gameHomeTeamId( game );
        if ( id != null ) {
            gameDTO.setHomeTeamId( id.toString() );
        }
        gameDTO.setHomeTeamName( gameHomeTeamName( game ) );
        gameDTO.setHomeTeamImageUrl( gameHomeTeamImageUrl( game ) );
        UUID id1 = gameAwayTeamId( game );
        if ( id1 != null ) {
            gameDTO.setAwayTeamId( id1.toString() );
        }
        gameDTO.setAwayTeamName( gameAwayTeamName( game ) );
        gameDTO.setAwayTeamImageUrl( gameAwayTeamImageUrl( game ) );
        UUID id2 = gameWeekId( game );
        if ( id2 != null ) {
            gameDTO.setWeekId( id2.toString() );
        }
        gameDTO.setWeekNumber( gameWeekWeekNumber( game ) );
        gameDTO.setYear( gameWeekYear( game ) );
        gameDTO.setStatusDisplay( statusToDisplay( game.getStatus() ) );
        gameDTO.setScoreDisplay( generateScoreDisplay( game ) );
        gameDTO.setIsCompleted( statusToCompleted( game.getStatus() ) );
        gameDTO.setWinnerName( getWinnerName( game ) );
        gameDTO.setId( game.getId() );
        gameDTO.setGameId( game.getGameId() );
        gameDTO.setHomeScore( game.getHomeScore() );
        gameDTO.setAwayScore( game.getAwayScore() );
        gameDTO.setDate( game.getDate() );
        gameDTO.setHomeTeamRank( game.getHomeTeamRank() );
        gameDTO.setAwayTeamRank( game.getAwayTeamRank() );
        gameDTO.setStatus( game.getStatus() );
        gameDTO.setCreatedAt( game.getCreatedAt() );
        gameDTO.setUpdatedAt( game.getUpdatedAt() );

        return gameDTO;
    }

    private UUID gameHomeTeamId(Game game) {
        if ( game == null ) {
            return null;
        }
        Team homeTeam = game.getHomeTeam();
        if ( homeTeam == null ) {
            return null;
        }
        UUID id = homeTeam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String gameHomeTeamName(Game game) {
        if ( game == null ) {
            return null;
        }
        Team homeTeam = game.getHomeTeam();
        if ( homeTeam == null ) {
            return null;
        }
        String name = homeTeam.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String gameHomeTeamImageUrl(Game game) {
        if ( game == null ) {
            return null;
        }
        Team homeTeam = game.getHomeTeam();
        if ( homeTeam == null ) {
            return null;
        }
        String imageUrl = homeTeam.getImageUrl();
        if ( imageUrl == null ) {
            return null;
        }
        return imageUrl;
    }

    private UUID gameAwayTeamId(Game game) {
        if ( game == null ) {
            return null;
        }
        Team awayTeam = game.getAwayTeam();
        if ( awayTeam == null ) {
            return null;
        }
        UUID id = awayTeam.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String gameAwayTeamName(Game game) {
        if ( game == null ) {
            return null;
        }
        Team awayTeam = game.getAwayTeam();
        if ( awayTeam == null ) {
            return null;
        }
        String name = awayTeam.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String gameAwayTeamImageUrl(Game game) {
        if ( game == null ) {
            return null;
        }
        Team awayTeam = game.getAwayTeam();
        if ( awayTeam == null ) {
            return null;
        }
        String imageUrl = awayTeam.getImageUrl();
        if ( imageUrl == null ) {
            return null;
        }
        return imageUrl;
    }

    private UUID gameWeekId(Game game) {
        if ( game == null ) {
            return null;
        }
        Week week = game.getWeek();
        if ( week == null ) {
            return null;
        }
        UUID id = week.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer gameWeekWeekNumber(Game game) {
        if ( game == null ) {
            return null;
        }
        Week week = game.getWeek();
        if ( week == null ) {
            return null;
        }
        Integer weekNumber = week.getWeekNumber();
        if ( weekNumber == null ) {
            return null;
        }
        return weekNumber;
    }

    private Integer gameWeekYear(Game game) {
        if ( game == null ) {
            return null;
        }
        Week week = game.getWeek();
        if ( week == null ) {
            return null;
        }
        Integer year = week.getYear();
        if ( year == null ) {
            return null;
        }
        return year;
    }
}
