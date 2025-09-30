package com.devsu.account.infrastructure.repository;

import com.devsu.account.Customer;
import com.devsu.account.infrastructure.repository.jpa.CustomerJpaRepository;
import com.devsu.account.infrastructure.repository.jpa.mapper.CustomerJpaMapper;
import com.devsu.account.usecase.port.outbound.CustomerRepository;
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
    public Optional<Customer> findById(UUID id) {
        var result = customerJpaRepository.findById(id);
        log.info("Found customer by id: {}", result);
        return result.map(mapper::toDomain);
    }
}
