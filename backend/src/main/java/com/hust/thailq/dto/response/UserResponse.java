package com.hust.thailq.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for User response.
 */
@Data
public class UserResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<RoleResponse> roles;
}
