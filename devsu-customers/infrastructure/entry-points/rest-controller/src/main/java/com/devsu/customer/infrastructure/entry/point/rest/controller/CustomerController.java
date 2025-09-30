package com.devsu.customer.infrastructure.entry.point.rest.controller;

import com.devsu.customer.Customer;
import com.devsu.customer.infrastructure.entry.point.rest.model.mapper.CustomerRestMapper;
import com.devsu.customer.infrastructure.entry.point.rest.model.request.CreateCustomerRequestDTO;
import com.devsu.customer.infrastructure.entry.point.rest.model.request.UpdateCustomerRequestDTO;
import com.devsu.customer.infrastructure.entry.point.rest.model.response.CustomerResponseDTO;
import com.devsu.customer.usecase.port.inbound.ChangeStatusCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.CreateCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.DeleteCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.GetCustomerUseCasePort;
import com.devsu.customer.usecase.port.inbound.UpdateCustomerUseCasePort;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CreateCustomerUseCasePort createCustomerUseCasePort;
    private final GetCustomerUseCasePort getCustteCustomerUseCasePort;
    private final DeleteCustomerUseCasePort delomerUseCasePort;
    private final UpdateCustomerUseCasePort updaeteCustomerUseCasePort;
    private final ChangeStatusCustomerUseCasePort changeStatusCustomerUseCasePort;
    private final CustomerRestMapper customerRestMapper;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @Validated @RequestBody CreateCustomerRequestDTO request) {
        log.info("Creating a new customer");
        Customer customer = customerRestMapper.toDomain(request);
        customer = createCustomerUseCasePort.create(customer);
        log.info("Customer created successfully: {}", customer);
        return ResponseEntity.created(URI.create(String.format("/%s", customer.getId())))
                .body(customerRestMapper.toResponse(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        log.info("Getting all customers");
        return ResponseEntity.ok(customerRestMapper.toResponse(getCustteCustomerUseCasePort.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        log.info("Getting customer by id: {}", id);
        Customer customer = getCustteCustomerUseCasePort.getById(id);
        log.info("Customer found: {}", customer);
        return ResponseEntity.ok(customerRestMapper.toResponse(customer));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable UUID id, @Validated @RequestBody UpdateCustomerRequestDTO request) {
        log.info("Updating customer with id: {}", id);
        Customer customer = customerRestMapper.toDomain(request);
        customer = updaeteCustomerUseCasePort.update(id, customer);
        log.info("Customer updated successfully: {}", customer);
        return ResponseEntity.ok(customerRestMapper.toResponse(customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        log.info("Deleting customer with id: {}", id);
        delomerUseCasePort.delete(id);
        log.info("Customer deleted successfully");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active/{active}")
    public ResponseEntity<CustomerResponseDTO> changeStatusCustomer(
            @PathVariable UUID id, @PathVariable boolean active) {
        log.info("Changing active flag of customer with id: {} to: {}", id, active);
        var customer = changeStatusCustomerUseCasePort.changeStatus(id, active);
        log.info("Customer active flag updated successfully: {}", customer);
        return ResponseEntity.ok(customerRestMapper.toResponse(customer));
    }
}
