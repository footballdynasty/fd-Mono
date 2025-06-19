package com.footballdynasty.mapper;

import com.footballdynasty.dto.UserDTO;
import com.footballdynasty.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TeamMapper.class})
public interface UserMapper {
    
    @Mapping(target = "selectedTeam", source = "selectedTeam")
    UserDTO toDTO(User user);
    
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "selectedTeam", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDTO userDTO);
}