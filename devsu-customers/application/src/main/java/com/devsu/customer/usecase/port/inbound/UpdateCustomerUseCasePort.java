package com.devsu.customer.usecase.port.inbound;

import com.devsu.customer.Customer;
import java.util.UUID;

public interface UpdateCustomerUseCasePort {
    Customer update(UUID id, Customer customer);
}
