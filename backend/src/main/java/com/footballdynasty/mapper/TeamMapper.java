package com.footballdynasty.mapper;

import com.footballdynasty.dto.TeamDTO;
import com.footballdynasty.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    
    TeamDTO toDTO(Team team);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "standings", ignore = true)
    @Mapping(target = "homeGames", ignore = true)
    @Mapping(target = "awayGames", ignore = true)
    Team toEntity(TeamDTO teamDTO);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "standings", ignore = true)
    @Mapping(target = "homeGames", ignore = true)
    @Mapping(target = "awayGames", ignore = true)
    void updateEntity(TeamDTO teamDTO, @MappingTarget Team team);
}