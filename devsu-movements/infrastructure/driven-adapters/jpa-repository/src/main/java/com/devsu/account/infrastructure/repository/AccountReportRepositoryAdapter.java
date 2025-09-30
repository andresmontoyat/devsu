package com.devsu.account.infrastructure.repository;

import com.devsu.account.pagination.PageRequest;
import com.devsu.account.pagination.PageResult;
import com.devsu.account.report.AccountStatusReportItem;
import com.devsu.account.usecase.port.outbound.AccountReportRepository;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReportRepositoryAdapter implements AccountReportRepository {

    @Override
    public PageResult<AccountStatusReportItem> findAccountsByPeriodAndClient(
            LocalDate fromDate, LocalDate toDate, UUID clientId, PageRequest pageRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
