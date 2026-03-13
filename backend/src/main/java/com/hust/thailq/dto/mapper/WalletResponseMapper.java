package com.hust.thailq.dto.mapper;

import com.hust.thailq.dto.response.UserResponse;
import com.hust.thailq.dto.response.WalletResponse;
import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.Wallet;
import com.hust.thailq.common.Constants;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Mapper used for mapping WalletResponse fields.
 */
@Mapper(componentModel = "spring", uses = {UserResponseMapper.class})
public interface WalletResponseMapper {

    Wallet toEntity(WalletResponse dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", source = "user")
    WalletResponse toDto(Wallet entity);

    @AfterMapping
    default void formatCreatedAt(@MappingTarget WalletResponse dto, Wallet entity) {
        LocalDateTime datetime = LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC);
        dto.setCreatedAt(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT).format(datetime));
    }
}
