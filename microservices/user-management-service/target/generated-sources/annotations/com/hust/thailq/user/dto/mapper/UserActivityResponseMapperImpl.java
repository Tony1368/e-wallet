package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.UserActivity;
import com.hust.thailq.user.dto.response.UserActivityResponse;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T17:33:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserActivityResponseMapperImpl implements UserActivityResponseMapper {

    @Autowired
    private UserResponseMapper userResponseMapper;
    @Autowired
    private UserSessionResponseMapper userSessionResponseMapper;

    @Override
    public UserActivityResponse toDto(UserActivity entity) {
        if ( entity == null ) {
            return null;
        }

        UserActivityResponse userActivityResponse = new UserActivityResponse();

        userActivityResponse.setUser( userResponseMapper.toDto( entity.getUser() ) );
        userActivityResponse.setSession( userSessionResponseMapper.toDto( entity.getSession() ) );
        userActivityResponse.setActivityId( entity.getActivityId() );
        userActivityResponse.setActivityType( entity.getActivityType() );
        userActivityResponse.setAmount( entity.getAmount() );
        userActivityResponse.setBrowser( entity.getBrowser() );
        userActivityResponse.setCity( entity.getCity() );
        userActivityResponse.setCountry( entity.getCountry() );
        userActivityResponse.setDescription( entity.getDescription() );
        userActivityResponse.setDeviceType( entity.getDeviceType() );
        userActivityResponse.setErrorMessage( entity.getErrorMessage() );
        userActivityResponse.setFromWalletIban( entity.getFromWalletIban() );
        userActivityResponse.setId( entity.getId() );
        userActivityResponse.setIpAddress( entity.getIpAddress() );
        userActivityResponse.setIsSuccessful( entity.getIsSuccessful() );
        userActivityResponse.setLatitude( entity.getLatitude() );
        userActivityResponse.setLongitude( entity.getLongitude() );
        userActivityResponse.setOperatingSystem( entity.getOperatingSystem() );
        userActivityResponse.setRegion( entity.getRegion() );
        userActivityResponse.setTimezone( entity.getTimezone() );
        userActivityResponse.setToWalletIban( entity.getToWalletIban() );
        userActivityResponse.setUserAgent( entity.getUserAgent() );

        formatActivityTime( userActivityResponse, entity );

        return userActivityResponse;
    }
}
