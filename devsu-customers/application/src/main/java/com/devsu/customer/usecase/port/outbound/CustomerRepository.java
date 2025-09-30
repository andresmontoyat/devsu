package com.devsu.customer.usecase.port.outbound;

import com.devsu.customer.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomerRepository is the interface for interacting with the Customer entity.
 */
public interface CustomerRepository {
    /**
     * Saves a new customer.
     * @param customer The customer to be saved.
     * @return The saved customer.
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by its ID.
     * @param id The ID of the customer to be found.
     * @return The found customer, or optional.empty() if the customer was not found.
     */
    Optional<Customer> findById(UUID id);

    /**
     * Find all customers.
     * @return All customers, or an empty list if none found.
     */
    List<Customer> getAll();

    /**
     * Updates an existing customer.
     * @param id The ID of the customer to be updated.
     * @param customer The updated customer.
     * @return The updated customer, or optional.empty() if the customer was not found.
     */
    Optional<Customer> update(UUID id, Customer customer);

    /**
     * Deletes a customer by its ID.
     * @param id The ID of the customer to be deleted.
     */
    void delete(UUID id);

    /**
     * Changes the active flag of a customer.
     * @param id The ID of the customer.
     * @param active The new active flag of the customer.
     * @return The updated customer, or optional.empty() if the customer was not found.
     */
    Optional<Customer> changeStatus(UUID id, boolean active);
}
