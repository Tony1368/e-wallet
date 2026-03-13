package com.hust.thailq.controller;

import com.hust.thailq.dto.response.UserResponse;
import com.hust.thailq.dto.mapper.UserResponseMapper;
import com.hust.thailq.domain.entity.User;
import com.hust.thailq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> response = users.stream().map(userResponseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
