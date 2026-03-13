package com.hust.thailq.transaction.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    PENDING("Pending"),
    SUCCESS("Success"),
    ERROR("Error"),
    FRAUD("Fraudulent");

    private String label;
}
