package com.devsu.account.usecase;

import com.devsu.account.Customer;
import com.devsu.account.usecase.port.inbound.CustomerUseCasePort;
import com.devsu.account.usecase.port.outbound.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerUseCase implements CustomerUseCasePort {
    private final CustomerRepository customerRepository;
    @Override
    public void createCustomer(Customer customer) {
        customerRepository.save(customer);
        log.info("Customer created: {}", customer);
    }
}
