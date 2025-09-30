package com.devsu.customer.infrastructure.repository;

import com.devsu.customer.Customer;
import com.devsu.customer.infrastructure.repository.jpa.CustomerJpaRepository;
import com.devsu.customer.infrastructure.repository.jpa.mapper.CustomerJpaMapper;
import com.devsu.customer.usecase.port.outbound.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerJpaMapper mapper;

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toEntity(customer);
        entity = customerJpaRepository.save(entity);
        log.info("Saved customer: {}", entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        var result = customerJpaRepository.findById(id);
        log.info("Found customer by id: {}", result);
        return result.map(mapper::toDomain);
    }

    @Override
    public List<Customer> getAll() {
        var result = customerJpaRepository.findAll();
        log.info("Found all customers: {}", result);
        return result.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Customer> update(UUID id, Customer customer) {
        var result = customerJpaRepository.findById(id).map((entity) -> {
            mapper.toEntity(customer, entity);
            var updated = customerJpaRepository.save(entity);
            log.info("Updated customer: {}", updated);
            return mapper.toDomain(updated);
        });
        log.info("Found customer to update by id: {}", result);
        return result;
    }

    @Override
    public void delete(UUID id) {
        customerJpaRepository.deleteById(id);
        log.info("Deleted customer by id: {}", id);
    }

    @Override
    public Optional<Customer> changeStatus(UUID id, boolean active) {
        var result = customerJpaRepository.findById(id).map((entity) -> {
            entity.setActive(active);
            var updated = customerJpaRepository.save(entity);
            log.info("Updated customer active flag: {}", updated);
            return mapper.toDomain(updated);
        });
        log.info("Found customer to update active flag by id: {}", result);
        return result;
    }
}
