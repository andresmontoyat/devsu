package com.devsu.account.infrastructure.repository.jpa;

import com.devsu.account.infrastructure.repository.jpa.entity.MovementEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementJpaRepository extends JpaRepository<MovementEntity, UUID> {
    List<MovementEntity> findAllByAccountId(UUID accountId);
}
