package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.UserSession;
import com.hust.thailq.user.dto.response.UserSessionResponse;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T23:07:18+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class UserSessionResponseMapperImpl implements UserSessionResponseMapper {

    @Autowired
    private UserResponseMapper userResponseMapper;

    @Override
    public UserSessionResponse toDto(UserSession entity) {
        if ( entity == null ) {
            return null;
        }

        UserSessionResponse userSessionResponse = new UserSessionResponse();

        userSessionResponse.setUser( userResponseMapper.toDto( entity.getUser() ) );
        userSessionResponse.setId( entity.getId() );
        userSessionResponse.setSessionId( entity.getSessionId() );
        userSessionResponse.setIpAddress( entity.getIpAddress() );
        userSessionResponse.setUserAgent( entity.getUserAgent() );
        userSessionResponse.setDeviceType( entity.getDeviceType() );
        userSessionResponse.setBrowser( entity.getBrowser() );
        userSessionResponse.setOperatingSystem( entity.getOperatingSystem() );
        userSessionResponse.setCountry( entity.getCountry() );
        userSessionResponse.setCity( entity.getCity() );
        userSessionResponse.setRegion( entity.getRegion() );
        userSessionResponse.setLatitude( entity.getLatitude() );
        userSessionResponse.setLongitude( entity.getLongitude() );
        userSessionResponse.setTimezone( entity.getTimezone() );
        userSessionResponse.setIsActive( entity.getIsActive() );

        formatTimes( userSessionResponse, entity );

        return userSessionResponse;
    }
}
