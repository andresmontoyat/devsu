package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Account;

public interface CreateAccountUseCasePort {
    Account create(Account account);
}
