package com.devsu.account.usecase;

import com.devsu.account.Account;
import com.devsu.account.Customer;
import com.devsu.account.Movement;
import com.devsu.account.enums.CustomerStatus;
import com.devsu.account.usecase.exception.InsufficientInitialBalanceException;
import com.devsu.account.usecase.port.inbound.ReceiveMovementUseCasePort;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import com.devsu.account.usecase.port.outbound.CustomerRepository;
import com.devsu.account.usecase.port.outbound.MovementPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MovementReceiveUseCase implements ReceiveMovementUseCasePort {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final MovementPublisher movementPublisher;

    @Override
    public void receive(Movement movement) {
        movement.setMovementDate(LocalDateTime.now());

        Customer customer = customerRepository
                .findById(movement.getCustomerId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Customer not found with id: " + movement.getCustomerId()));

        if (Boolean.FALSE.equals(customer.getActive())) {
            throw new IllegalArgumentException("Customer is not active. Movement cannot be processed.");
        }

        UUID accountId = movement.getAccountId();
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));

        if (Boolean.FALSE.equals(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active. Movement cannot be processed.");
        }

        BigDecimal initialBalance = account.getInitialBalance();
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) == 0) {
            throw new InsufficientInitialBalanceException(
                    "Account has zero initial balance. Movement cannot be processed.");
        }

        log.info("Movement accepted for background processing: {}", movement);
        movementPublisher.movementReceived(movement);
    }
}
