package com.footballdynasty.mapper;

import com.footballdynasty.dto.StandingCreateDTO;
import com.footballdynasty.dto.StandingDTO;
import com.footballdynasty.dto.StandingUpdateDTO;
import com.footballdynasty.entity.Standing;
import com.footballdynasty.entity.Team;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-18T20:15:33-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Homebrew)"
)
@Component
public class StandingMapperImpl implements StandingMapper {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public StandingDTO toDTO(Standing standing) {
        if ( standing == null ) {
            return null;
        }

        StandingDTO standingDTO = new StandingDTO();

        standingDTO.setWinPercentage( standing.getWinPercentage() );
        standingDTO.setTotalGames( standing.getTotalGames() );
        standingDTO.setConferenceWinPercentage( standing.getConferenceWinPercentage() );
        standingDTO.setTotalConferenceGames( standing.getTotalConferenceGames() );
        standingDTO.setId( standing.getId() );
        standingDTO.setTeam( teamMapper.toDTO( standing.getTeam() ) );
        standingDTO.setYear( standing.getYear() );
        standingDTO.setWins( standing.getWins() );
        standingDTO.setLosses( standing.getLosses() );
        standingDTO.setConferenceWins( standing.getConferenceWins() );
        standingDTO.setConferenceLosses( standing.getConferenceLosses() );
        standingDTO.setRank( standing.getRank() );
        standingDTO.setConferenceRank( standing.getConferenceRank() );
        standingDTO.setReceivingVotes( standing.getReceivingVotes() );
        standingDTO.setCreatedAt( standing.getCreatedAt() );
        standingDTO.setUpdatedAt( standing.getUpdatedAt() );

        return standingDTO;
    }

    @Override
    public Standing toEntity(StandingCreateDTO createDTO, Team team) {
        if ( createDTO == null && team == null ) {
            return null;
        }

        Standing standing = new Standing();

        if ( createDTO != null ) {
            standing.setYear( createDTO.getYear() );
            standing.setWins( createDTO.getWins() );
            standing.setLosses( createDTO.getLosses() );
            standing.setRank( createDTO.getRank() );
            standing.setConferenceWins( createDTO.getConferenceWins() );
            standing.setConferenceLosses( createDTO.getConferenceLosses() );
            standing.setConferenceRank( createDTO.getConferenceRank() );
            standing.setReceivingVotes( createDTO.getReceivingVotes() );
        }
        standing.setTeam( team );

        return standing;
    }

    @Override
    public void updateEntity(StandingUpdateDTO updateDTO, Standing standing) {
        if ( updateDTO == null ) {
            return;
        }

        standing.setWins( updateDTO.getWins() );
        standing.setLosses( updateDTO.getLosses() );
        standing.setRank( updateDTO.getRank() );
        standing.setConferenceWins( updateDTO.getConferenceWins() );
        standing.setConferenceLosses( updateDTO.getConferenceLosses() );
        standing.setConferenceRank( updateDTO.getConferenceRank() );
        standing.setReceivingVotes( updateDTO.getReceivingVotes() );
    }

    @Override
    public Standing createEntity(Team team, Integer year) {
        if ( team == null && year == null ) {
            return null;
        }

        Standing standing = new Standing();

        standing.setTeam( team );
        standing.setYear( year );
        standing.setWins( 0 );
        standing.setLosses( 0 );
        standing.setConferenceWins( 0 );
        standing.setConferenceLosses( 0 );
        standing.setReceivingVotes( 0 );

        return standing;
    }
}
