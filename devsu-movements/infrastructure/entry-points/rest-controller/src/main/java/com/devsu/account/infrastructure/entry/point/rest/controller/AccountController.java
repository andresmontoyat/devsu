package com.devsu.account.infrastructure.entry.point.rest.controller;

import com.devsu.account.Account;
import com.devsu.account.infrastructure.entry.point.rest.model.mapper.AccountRestMapper;
import com.devsu.account.infrastructure.entry.point.rest.model.request.CreateAccountRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.request.UpdateAccountRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.response.AccountResponseDTO;
import com.devsu.account.usecase.port.inbound.ChangeStatusAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.CreateAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.GetAccountUseCasePort;
import com.devsu.account.usecase.port.inbound.UpdateAccountUseCasePort;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCasePort createAccountUseCasePort;
    private final GetAccountUseCasePort getAccountUseCasePort;
    private final UpdateAccountUseCasePort updateAccountUseCasePort;
    private final ChangeStatusAccountUseCasePort changeStatusAccountUseCasePort;
    private final AccountRestMapper accountRestMapper;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Validated @RequestBody CreateAccountRequestDTO request) {
        log.info("Creating a new account");
        Account account = accountRestMapper.toDomain(request);
        account = createAccountUseCasePort.create(account);
        log.info("Account created successfully: {}", account);
        return ResponseEntity.created(URI.create(String.format("/%s", account.getId())))
                .body(accountRestMapper.toResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        log.info("Getting all accounts");
        return ResponseEntity.ok(accountRestMapper.toResponse(getAccountUseCasePort.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID id) {
        log.info("Getting account by id: {}", id);
        Account account = getAccountUseCasePort.getById(id);
        log.info("Account found: {}", account);
        return ResponseEntity.ok(accountRestMapper.toResponse(account));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable UUID id, @Validated @RequestBody UpdateAccountRequestDTO request) {
        log.info("Updating account with id: {}", id);
        Account account = accountRestMapper.toDomain(request);
        account = updateAccountUseCasePort.update(id, account);
        log.info("Account updated successfully: {}", account);
        return ResponseEntity.ok(accountRestMapper.toResponse(account));
    }

    @PatchMapping("/{id}/active/{active}")
    public ResponseEntity<AccountResponseDTO> changeStatusCustomer(
            @PathVariable UUID id, @PathVariable boolean active) {
        log.info("Changing active flag of account with id: {} to: {}", id, active);
        var customer = changeStatusAccountUseCasePort.changeStatus(id, active);
        log.info("Account active flag updated successfully: {}", customer);
        return ResponseEntity.ok(accountRestMapper.toResponse(customer));
    }
}
