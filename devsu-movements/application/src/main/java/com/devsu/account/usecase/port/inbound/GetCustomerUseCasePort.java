package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Customer;
import java.util.UUID;

public interface GetCustomerUseCasePort {
    Customer getById(UUID id);
}
