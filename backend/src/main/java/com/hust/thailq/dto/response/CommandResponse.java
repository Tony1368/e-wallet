package com.hust.thailq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for returning id value of the saved entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponse {
    private Long id;
    private String message;
}
