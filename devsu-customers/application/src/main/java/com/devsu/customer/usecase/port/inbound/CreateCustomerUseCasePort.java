package com.devsu.customer.usecase.port.inbound;

import com.devsu.customer.Customer;

public interface CreateCustomerUseCasePort {
    Customer create(Customer customer);
}
