package com.hust.thailq.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {

    ROLE_USER("User"),
    ROLE_ADMIN("Admin"),
    ROLE_ACCOUNTANT("Accountant"),
    ROLE_CUSTOMER("Customer");

    private String label;
}
