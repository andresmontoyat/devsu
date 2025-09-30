package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Account;
import java.util.List;
import java.util.UUID;

public interface GetAccountUseCasePort {
    Account getById(UUID id);

    List<Account> getAll();
}
