package com.hust.thailq.user.dto.mapper;

import com.hust.thailq.user.domain.entity.UserSession;
import com.hust.thailq.user.dto.response.UserSessionResponse;
import com.hust.thailq.common.Constants;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Mapper used for mapping UserSession entity to UserSessionResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserResponseMapper.class})
public interface UserSessionResponseMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "loginTime", ignore = true)
    @Mapping(target = "logoutTime", ignore = true)
    UserSessionResponse toDto(UserSession entity);

    @AfterMapping
    default void formatTimes(@MappingTarget UserSessionResponse dto, UserSession entity) {
        // Format login time using system default timezone
        if (entity.getLoginTime() != null) {
            dto.setLoginTime(formatToLocalTime(entity.getLoginTime()));
        }
        
        // Format logout time using system default timezone
        if (entity.getLogoutTime() != null) {
            dto.setLogoutTime(formatToLocalTime(entity.getLogoutTime()));
        }
    }

    default String formatToLocalTime(java.time.Instant instant) {
        LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT).format(datetime);
    }
} 