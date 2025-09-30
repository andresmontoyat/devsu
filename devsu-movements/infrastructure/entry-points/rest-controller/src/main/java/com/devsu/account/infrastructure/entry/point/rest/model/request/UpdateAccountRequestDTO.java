package com.devsu.account.infrastructure.entry.point.rest.model.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateAccountRequestDTO {

    private String accountType;

    @Positive
    private BigDecimal initialBalance;
}
