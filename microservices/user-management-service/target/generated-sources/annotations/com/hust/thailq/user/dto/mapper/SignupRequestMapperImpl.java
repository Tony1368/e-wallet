package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.User;
import com.hust.thailq.user.dto.request.SignupRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-13T14:09:25+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class SignupRequestMapperImpl extends SignupRequestMapper {

    @Override
    public User toEntity(SignupRequest dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setId( dto.getId() );

        user.setFirstName( org.apache.commons.text.WordUtils.capitalizeFully(dto.getFirstName()) );
        user.setLastName( org.apache.commons.text.WordUtils.capitalizeFully(dto.getLastName()) );
        user.setUsername( dto.getUsername().trim().toLowerCase() );
        user.setEmail( dto.getEmail().trim().toLowerCase() );

        setToEntityFields( user, dto );

        return user;
    }
}
