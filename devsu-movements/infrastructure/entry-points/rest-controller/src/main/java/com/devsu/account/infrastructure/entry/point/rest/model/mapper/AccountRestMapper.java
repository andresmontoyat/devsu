package com.devsu.account.infrastructure.entry.point.rest.model.mapper;

import com.devsu.account.Account;
import com.devsu.account.infrastructure.entry.point.rest.model.request.CreateAccountRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.request.UpdateAccountRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.response.AccountResponseDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AccountRestMapper {

    Account toDomain(CreateAccountRequestDTO request);

    Account toDomain(UpdateAccountRequestDTO request);

    AccountResponseDTO toResponse(Account account);

    List<AccountResponseDTO> toResponse(List<Account> accounts);
}
