package com.devsu.account.infrastructure.repository;

import com.devsu.account.Movement;
import com.devsu.account.infrastructure.repository.jpa.MovementJpaRepository;
import com.devsu.account.infrastructure.repository.jpa.mapper.MovementJpaMapper;
import com.devsu.account.usecase.port.outbound.MovementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovementRepositoryAdapter implements MovementRepository {

    private final MovementJpaRepository movementJpaRepository;
    private final MovementJpaMapper mapper;

    @Override
    public Movement save(Movement movement) {
        var entity = mapper.toEntity(movement);
        entity = movementJpaRepository.save(entity);
        log.info("Saved movement: {}", entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Movement> findById(UUID id) {
        var result = movementJpaRepository.findById(id);
        log.info("Found movement by id: {}", result);
        return result.map(mapper::toDomain);
    }

    @Override
    public List<Movement> getAllByAccountId(UUID accountId) {
        var result = movementJpaRepository.findAllByAccountId(accountId);
        log.info("Found movements by accountId: {} -> {}", accountId, result);
        return result.stream().map(mapper::toDomain).toList();
    }
}
