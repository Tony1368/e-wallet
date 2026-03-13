package com.hust.thailq.dto.mapper;

import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.Wallet;
import com.hust.thailq.dto.response.UserResponse;
import com.hust.thailq.dto.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "user", expression = "java(mapUserToUserResponse(wallet.getUser()))")
    @Mapping(target = "status", source = "status")
    WalletResponse toDto(Wallet wallet);

    UserResponse mapUserToUserResponse(User user);
} 