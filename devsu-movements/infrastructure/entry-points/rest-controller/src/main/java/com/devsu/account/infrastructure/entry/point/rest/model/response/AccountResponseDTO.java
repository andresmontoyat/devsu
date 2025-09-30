package com.devsu.account.infrastructure.entry.point.rest.model.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class AccountResponseDTO {
    private UUID id;
    private Long accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private Boolean status;
    private UUID customerId;
}
