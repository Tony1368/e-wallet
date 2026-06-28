package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.UserActivity;
import com.hust.thailq.user.dto.response.UserActivityResponse;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T23:07:18+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
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
        userActivityResponse.setId( entity.getId() );
        userActivityResponse.setActivityId( entity.getActivityId() );
        userActivityResponse.setActivityType( entity.getActivityType() );
        userActivityResponse.setDescription( entity.getDescription() );
        userActivityResponse.setAmount( entity.getAmount() );
        userActivityResponse.setFromWalletIban( entity.getFromWalletIban() );
        userActivityResponse.setToWalletIban( entity.getToWalletIban() );
        userActivityResponse.setIpAddress( entity.getIpAddress() );
        userActivityResponse.setUserAgent( entity.getUserAgent() );
        userActivityResponse.setDeviceType( entity.getDeviceType() );
        userActivityResponse.setBrowser( entity.getBrowser() );
        userActivityResponse.setOperatingSystem( entity.getOperatingSystem() );
        userActivityResponse.setCountry( entity.getCountry() );
        userActivityResponse.setCity( entity.getCity() );
        userActivityResponse.setRegion( entity.getRegion() );
        userActivityResponse.setLatitude( entity.getLatitude() );
        userActivityResponse.setLongitude( entity.getLongitude() );
        userActivityResponse.setTimezone( entity.getTimezone() );
        userActivityResponse.setIsSuccessful( entity.getIsSuccessful() );
        userActivityResponse.setErrorMessage( entity.getErrorMessage() );

        formatActivityTime( userActivityResponse, entity );

        return userActivityResponse;
    }
}
