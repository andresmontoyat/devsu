package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Account;
import java.util.UUID;

public interface UpdateAccountUseCasePort {
    Account update(UUID id, Account account);
}
