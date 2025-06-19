package com.footballdynasty.mapper;

import com.footballdynasty.dto.TeamDTO;
import com.footballdynasty.entity.Team;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-18T20:15:33-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Homebrew)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public TeamDTO toDTO(Team team) {
        if ( team == null ) {
            return null;
        }

        TeamDTO teamDTO = new TeamDTO();

        teamDTO.setId( team.getId() );
        teamDTO.setName( team.getName() );
        teamDTO.setCoach( team.getCoach() );
        teamDTO.setUsername( team.getUsername() );
        teamDTO.setConference( team.getConference() );
        teamDTO.setIsHuman( team.getIsHuman() );
        teamDTO.setImageUrl( team.getImageUrl() );
        teamDTO.setCreatedAt( team.getCreatedAt() );
        teamDTO.setUpdatedAt( team.getUpdatedAt() );

        return teamDTO;
    }

    @Override
    public Team toEntity(TeamDTO teamDTO) {
        if ( teamDTO == null ) {
            return null;
        }

        Team team = new Team();

        team.setName( teamDTO.getName() );
        team.setCoach( teamDTO.getCoach() );
        team.setUsername( teamDTO.getUsername() );
        team.setConference( teamDTO.getConference() );
        team.setIsHuman( teamDTO.getIsHuman() );
        team.setImageUrl( teamDTO.getImageUrl() );

        return team;
    }

    @Override
    public void updateEntity(TeamDTO teamDTO, Team team) {
        if ( teamDTO == null ) {
            return;
        }

        team.setName( teamDTO.getName() );
        team.setCoach( teamDTO.getCoach() );
        team.setUsername( teamDTO.getUsername() );
        team.setConference( teamDTO.getConference() );
        team.setIsHuman( teamDTO.getIsHuman() );
        team.setImageUrl( teamDTO.getImageUrl() );
    }
}
