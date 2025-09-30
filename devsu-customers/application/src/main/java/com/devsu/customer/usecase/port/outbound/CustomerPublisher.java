package com.devsu.customer.usecase.port.outbound;

import com.devsu.customer.Customer;
import java.util.UUID;

public interface CustomerPublisher {
    void customerCreated(Customer result);

    void customerDeleted(UUID id);

    void customerStatusChanged(UUID id, boolean active);
}
