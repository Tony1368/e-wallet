package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.Role;
import com.hust.thailq.user.domain.entity.User;
import com.hust.thailq.user.dto.response.RoleResponse;
import com.hust.thailq.user.dto.response.UserResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T06:18:48+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserResponseMapperImpl implements UserResponseMapper {

    @Override
    public UserResponse toDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setRoles( roleSetToRoleResponseList( entity.getRoles() ) );
        userResponse.setEmail( entity.getEmail() );
        userResponse.setFirstName( entity.getFirstName() );
        userResponse.setId( entity.getId() );
        userResponse.setLastName( entity.getLastName() );
        userResponse.setUsername( entity.getUsername() );

        return userResponse;
    }

    protected RoleResponse roleToRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse roleResponse = new RoleResponse();

        roleResponse.setId( role.getId() );
        roleResponse.setType( role.getType() );

        return roleResponse;
    }

    protected List<RoleResponse> roleSetToRoleResponseList(Set<Role> set) {
        if ( set == null ) {
            return null;
        }

        List<RoleResponse> list = new ArrayList<RoleResponse>( set.size() );
        for ( Role role : set ) {
            list.add( roleToRoleResponse( role ) );
        }

        return list;
    }
}
