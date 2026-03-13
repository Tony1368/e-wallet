package com.hust.thailq.dto.mapper;

import com.hust.thailq.dto.response.TransactionResponse;
import com.hust.thailq.domain.entity.Transaction;
import com.hust.thailq.common.Constants;
import com.hust.thailq.domain.entity.Type;
import com.hust.thailq.dto.response.TypeResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Mapper used for mapping TransactionResponse fields.
 */
@Mapper(componentModel = "spring", uses = {WalletResponseMapper.class})
public interface TransactionResponseMapper {

    Transaction toEntity(TransactionResponse dto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "fromWallet", source = "fromWallet")
    @Mapping(target = "toWallet", source = "toWallet")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "ipAddress", target = "ipAddress")
    @Mapping(source = "userAgent", target = "userAgent")
    @Mapping(source = "deviceType", target = "deviceType")
    @Mapping(source = "browser", target = "browser")
    @Mapping(source = "operatingSystem", target = "operatingSystem")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "timezone", target = "timezone")
    TransactionResponse toDto(Transaction entity);

    @AfterMapping
    default void formatCreatedAtAndType(@MappingTarget TransactionResponse dto, Transaction entity) {
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(formatToLocalTime(entity.getCreatedAt()));
        }
        if (entity.getType() != null) {
            Type typeEntity = entity.getType();
            TypeResponse typeResponse = new TypeResponse();
            typeResponse.setId(typeEntity.getId());
            typeResponse.setName(getVietnameseTypeName(typeEntity.getName()));
            typeResponse.setDescription(typeEntity.getDescription());
            dto.setType(typeResponse);
        }
    }

    default String getVietnameseTypeName(String typeName) {
        if (typeName == null) return "";
        return switch (typeName.toUpperCase()) {
            case "TRANSFER" -> "Chuyển tiền";
            case "WITHDRAW" -> "Rút tiền";
            case "ADD_FUNDS", "DEPOSIT" -> "Nạp tiền";
            case "INIT_WALLET" -> "Khởi tạo ví";
            default -> typeName;
        };
    }

    default String formatToLocalTime(java.time.Instant instant) {
        LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT).format(datetime);
    }
}
