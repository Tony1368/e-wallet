package com.hust.thailq.dto.mapper;

import com.hust.thailq.domain.entity.UserActivity;
import com.hust.thailq.dto.response.UserActivityResponse;
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
 * Mapper used for mapping UserActivity entity to UserActivityResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserResponseMapper.class, UserSessionResponseMapper.class})
public interface UserActivityResponseMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "session", source = "session")
    @Mapping(target = "activityTime", ignore = true)
    UserActivityResponse toDto(UserActivity entity);

    @AfterMapping
    default void formatActivityTime(@MappingTarget UserActivityResponse dto, UserActivity entity) {
        // Format activity time using system default timezone
        if (entity.getActivityTime() != null) {
            dto.setActivityTime(formatToLocalTime(entity.getActivityTime()));
        }
    }

    default String formatToLocalTime(java.time.Instant instant) {
        LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT).format(datetime);
    }
} 