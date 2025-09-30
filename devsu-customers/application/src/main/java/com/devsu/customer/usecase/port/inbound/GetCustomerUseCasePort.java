package com.devsu.customer.usecase.port.inbound;

import com.devsu.customer.Customer;
import java.util.List;
import java.util.UUID;

public interface GetCustomerUseCasePort {

    Customer getById(UUID id);

    List<Customer> getAll();
}
