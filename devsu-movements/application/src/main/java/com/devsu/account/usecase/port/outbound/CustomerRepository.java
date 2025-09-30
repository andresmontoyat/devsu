package com.devsu.account.usecase.port.outbound;

import com.devsu.account.Customer;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomerRepository is the interface for interacting with the Customer entity.
 */
public interface CustomerRepository {

    /**
     * Finds a customer by its ID.
     * @param id The ID of the customer to be found.
     * @return The found customer, or optional.empty() if the customer was not found.
     */
    Optional<Customer> findById(UUID id);
}
