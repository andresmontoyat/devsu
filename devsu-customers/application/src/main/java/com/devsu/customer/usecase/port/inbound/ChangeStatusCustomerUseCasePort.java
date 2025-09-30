package com.devsu.customer.usecase.port.inbound;

import com.devsu.customer.Customer;
import java.util.UUID;

public interface ChangeStatusCustomerUseCasePort {
    Customer changeStatus(UUID id, boolean active);
}
