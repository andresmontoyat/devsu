package com.devsu.account.infrastructure.repository.jpa;

import com.devsu.account.infrastructure.repository.jpa.entity.AccountEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {}
