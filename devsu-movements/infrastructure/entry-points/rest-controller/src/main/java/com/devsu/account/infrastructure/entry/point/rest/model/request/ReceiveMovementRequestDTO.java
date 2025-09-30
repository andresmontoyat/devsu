package com.devsu.account.infrastructure.entry.point.rest.model.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class ReceiveMovementRequestDTO {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID accountId;

    @NotNull
    private BigDecimal value;
}
