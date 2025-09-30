package com.devsu.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movement {
    private UUID id;
    private UUID customerId;
    private UUID accountId;
    private LocalDateTime movementDate;
    private BigDecimal value;
    private BigDecimal balance;
}
