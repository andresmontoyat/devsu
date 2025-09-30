package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Account;
import java.util.UUID;

public interface ChangeStatusAccountUseCasePort {
    Account changeStatus(UUID id, boolean active);
}
