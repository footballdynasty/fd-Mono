package com.footballdynasty.mapper;

import com.footballdynasty.dto.UserDTO;
import com.footballdynasty.entity.Role;
import com.footballdynasty.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-20T16:51:52-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Homebrew)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setSelectedTeam( teamMapper.toDTO( user.getSelectedTeam() ) );
        userDTO.setId( user.getId() );
        userDTO.setUsername( user.getUsername() );
        userDTO.setEmail( user.getEmail() );
        userDTO.setSelectedTeamId( user.getSelectedTeamId() );
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            userDTO.setRoles( new LinkedHashSet<Role>( set ) );
        }
        userDTO.setIsActive( user.getIsActive() );
        userDTO.setCreatedAt( user.getCreatedAt() );
        userDTO.setUpdatedAt( user.getUpdatedAt() );

        return userDTO;
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User user = new User();

        user.setId( userDTO.getId() );
        user.setUsername( userDTO.getUsername() );
        user.setEmail( userDTO.getEmail() );
        user.setSelectedTeamId( userDTO.getSelectedTeamId() );
        Set<Role> set = userDTO.getRoles();
        if ( set != null ) {
            user.setRoles( new LinkedHashSet<Role>( set ) );
        }
        user.setIsActive( userDTO.getIsActive() );

        return user;
    }
}
