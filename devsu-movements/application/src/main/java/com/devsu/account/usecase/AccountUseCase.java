package com.devsu.account.usecase;

import com.devsu.account.Account;
import com.devsu.account.usecase.port.inbound.ChangeStatusAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.CreateAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.GetAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.UpdateAccountUseCasePort;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AccountUseCase
        implements CreateAccountUseCasePort,
                GetAccountUseCasePort,
                UpdateAccountUseCasePort,
                ChangeStatusAccountUseCasePort {

    private final AccountRepository accountRepository;

    @Override
    public Account create(Account account) {
        account.setStatus(Boolean.TRUE);
        var result = accountRepository.save(account);
        log.info("Account created successfully: {}", result);
        return result;
    }

    @Override
    public Account getById(UUID id) {
        return accountRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.getAll();
    }

    @Override
    public Account update(UUID id, Account account) {
        return accountRepository
                .update(id, account)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));
    }

    @Override
    public Account changeStatus(UUID id, boolean active) {
        var updated = accountRepository
                .changeStatus(id, active)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        log.info("Accoint active flag updated successfully: {}", updated);
        return updated;
    }
}
