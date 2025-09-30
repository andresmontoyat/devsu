package com.devsu.account.usecase.port.outbound;

import com.devsu.account.Movement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovementRepository {
    Movement save(Movement movement);

    Optional<Movement> findById(UUID id);

    List<Movement> getAllByAccountId(UUID accountId);
}
