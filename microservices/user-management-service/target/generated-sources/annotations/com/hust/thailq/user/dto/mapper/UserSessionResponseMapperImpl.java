package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.UserSession;
import com.hust.thailq.user.dto.response.UserSessionResponse;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-12T09:33:24+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
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
        userSessionResponse.setBrowser( entity.getBrowser() );
        userSessionResponse.setCity( entity.getCity() );
        userSessionResponse.setCountry( entity.getCountry() );
        userSessionResponse.setDeviceType( entity.getDeviceType() );
        userSessionResponse.setId( entity.getId() );
        userSessionResponse.setIpAddress( entity.getIpAddress() );
        userSessionResponse.setIsActive( entity.getIsActive() );
        userSessionResponse.setLatitude( entity.getLatitude() );
        userSessionResponse.setLongitude( entity.getLongitude() );
        userSessionResponse.setOperatingSystem( entity.getOperatingSystem() );
        userSessionResponse.setRegion( entity.getRegion() );
        userSessionResponse.setSessionId( entity.getSessionId() );
        userSessionResponse.setTimezone( entity.getTimezone() );
        userSessionResponse.setUserAgent( entity.getUserAgent() );

        formatTimes( userSessionResponse, entity );

        return userSessionResponse;
    }
}
