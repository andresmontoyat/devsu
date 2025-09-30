package com.devsu.account.usecase.port.outbound;

import com.devsu.account.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(UUID id);

    List<Account> getAll();

    Optional<Account> update(UUID id, Account account);

    Optional<Account> changeStatus(UUID id, boolean active);
}
