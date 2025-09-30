package com.devsu.customer.usecase.port.inbound;

import java.util.UUID;

public interface DeleteCustomerUseCasePort {
    void delete(UUID id);
}
