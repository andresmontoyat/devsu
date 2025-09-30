package com.devsu.account.infrastructure.repository.jpa.mapper;

import com.devsu.account.Account;
import com.devsu.account.infrastructure.repository.jpa.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AccountJpaMapper {
    AccountEntity toEntity(Account account);

    void toEntity(Account account, @MappingTarget AccountEntity entity);

    Account toDomain(AccountEntity entity);
}
