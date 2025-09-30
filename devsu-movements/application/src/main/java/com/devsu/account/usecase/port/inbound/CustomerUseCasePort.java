package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Customer;

public interface CustomerUseCasePort {

    void createCustomer(Customer customer);
}
