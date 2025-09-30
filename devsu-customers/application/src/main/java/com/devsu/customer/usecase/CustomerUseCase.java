package com.devsu.customer.usecase;

import com.devsu.customer.Customer;
import com.devsu.customer.usecase.port.inbound.ChangeStatusCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.CreateCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.DeleteCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.GetCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.UpdateCustomerUseCasePort;
import com.devsu.customer.usecase.port.outbound.CustomerPublisher;
import com.devsu.customer.usecase.port.outbound.CustomerRepository;
import com.devsu.customer.usecase.port.outbound.SecurityUtil;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomerUseCase
        implements CreateCustomerUseCasePort,
                DeleteCustomerUseCasePort,
                GetCustomerUseCasePort,
                UpdateCustomerUseCasePort,
                ChangeStatusCustomerUseCasePort {
    private final CustomerRepository customerRepository;
    private final CustomerPublisher customerPublisher;
    private final SecurityUtil securityUtil;

    @Override
    public Customer create(Customer customer) {
        customer.setActive(Boolean.TRUE);
        customer.setPassword(securityUtil.hashPassword(customer.getPassword()));

        var result = customerRepository.save(customer);
        log.info("Customer created successfully: {}", result);
        customerPublisher.customerCreated(result);
        return result;
    }

    @Override
    public void delete(UUID id) {
        customerRepository.delete(id);
        log.info("Customer deleted successfully");
        customerPublisher.customerDeleted(id);
    }

    @Override
    public Customer getById(UUID id) {
        var result = customerRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        log.info("Found customer by id: {}", result);
        return result;
    }

    @Override
    public List<Customer> getAll() {
        var result = customerRepository.getAll();
        log.info("Found all customers: {}", result);
        return result;
    }

    @Override
    public Customer update(UUID id, Customer customer) {
        var updated = customerRepository
                .update(id, customer)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        return updated;
    }

    @Override
    public Customer changeStatus(UUID id, boolean active) {
        var updated = customerRepository
                .changeStatus(id, active)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        log.info("Customer active flag updated successfully: {}", updated);
        customerPublisher.customerStatusChanged(id, active);
        return updated;
    }
}
