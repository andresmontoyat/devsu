package com.devsu.account.usecase;

import com.devsu.account.Account;
import com.devsu.account.Movement;
import com.devsu.account.usecase.port.inbound.MovementProcessUseCasePort;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import com.devsu.account.usecase.port.outbound.MovementRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MovementProcessUseCase implements MovementProcessUseCasePort {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;

    @Override
    public void process(Movement movement) {

        Account account = accountRepository
                .findById(movement.getAccountId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Account not found with id: " + movement.getAccountId()));

        movement.setBalance(account.getInitialBalance());
        var saved = movementRepository.save(movement);
        log.info("Movement persisted: {}", saved);

        BigDecimal newBalance = account.getInitialBalance().add(saved.getValue());
        account.setInitialBalance(newBalance);

        Account updated = accountRepository.update(account.getId(), account).get();
        log.info("Account balance updated: {}", updated);
    }
}
