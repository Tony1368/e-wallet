package com.hust.thailq.user.dto.response;

import com.hust.thailq.user.domain.enums.RoleType;
import lombok.Data;

/**
 * Data Transfer Object for Role response.
 */
@Data
public class RoleResponse {

    private Long id;
    private RoleType type;
}
