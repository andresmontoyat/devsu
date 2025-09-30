package com.devsu.account.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusReportItem {
    private UUID accountId;
    private Long accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private Boolean status;
    private UUID customerId;

    // Customer fields (flattened)
    private String customerName;
    private String customerIdentification;

    // Movement fields (optional for report period)
    private LocalDateTime lastMovementDate;
    private BigDecimal lastBalance;
}
