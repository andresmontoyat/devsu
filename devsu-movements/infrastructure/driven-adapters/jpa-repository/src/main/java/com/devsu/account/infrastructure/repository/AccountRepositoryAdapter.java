package com.devsu.account.infrastructure.repository;

import com.devsu.account.Account;
import com.devsu.account.infrastructure.repository.jpa.AccountJpaRepository;
import com.devsu.account.infrastructure.repository.jpa.mapper.AccountJpaMapper;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountJpaMapper mapper;

    @Override
    public Account save(Account account) {
        var entity = mapper.toEntity(account);
        entity = accountJpaRepository.save(entity);
        log.info("Saved account: {}", entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        var result = accountJpaRepository.findById(id);
        log.info("Found account by id: {}", result);
        return result.map(mapper::toDomain);
    }

    @Override
    public List<Account> getAll() {
        var result = accountJpaRepository.findAll();
        log.info("Found all accounts: {}", result);
        return result.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Account> update(UUID id, Account account) {
        var result = accountJpaRepository.findById(id).map(entity -> {
            mapper.toEntity(account, entity);
            var updated = accountJpaRepository.save(entity);
            log.info("Updated account: {}", updated);
            return mapper.toDomain(updated);
        });
        log.info("Found account to update by id: {}", result);
        return result;
    }

    @Override
    public Optional<Account> changeStatus(UUID id, boolean active) {
        var result = accountJpaRepository.findById(id).map((entity) -> {
            entity.setStatus(active);
            var updated = accountJpaRepository.save(entity);
            log.info("Updated customer active flag: {}", updated);
            return mapper.toDomain(updated);
        });
        log.info("Found customer to update active flag by id: {}", result);
        return result;
    }
}
