package com.hust.thailq.dto.mapper;

import com.hust.thailq.domain.entity.User;
import com.hust.thailq.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper used for mapping User entity to UserResponse.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserResponseMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponse toDto(User entity);
} 