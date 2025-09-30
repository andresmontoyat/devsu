package com.devsu.account.infrastructure.entry.point.rest.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateAccountRequestDTO {

    @NotNull
    @Positive
    private Long accountNumber;

    @NotNull
    private String accountType;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;

    @NotNull
    private UUID customerId;
}
