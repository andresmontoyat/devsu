package com.devsu.account;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    private UUID id;
    private Long accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private Boolean status;
    private UUID customerId;
}
