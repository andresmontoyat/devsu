package com.devsu.account.usecase;

import com.devsu.account.pagination.PageRequest;
import com.devsu.account.pagination.PageResult;
import com.devsu.account.report.AccountStatusReportItem;
import com.devsu.account.usecase.port.inbound.StatementReportUseCasePort;
import com.devsu.account.usecase.port.outbound.AccountReportRepository;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StatementReportUseCase implements StatementReportUseCasePort {

    private final AccountReportRepository accountReportRepository;

    @Override
    public PageResult<AccountStatusReportItem> getReport(
            LocalDate fromDate, LocalDate toDate, UUID clientId, PageRequest pageRequest) {
        log.info("Generating account status report from {} to {} for client {}", fromDate, toDate, clientId);
        return accountReportRepository.findAccountsByPeriodAndClient(fromDate, toDate, clientId, pageRequest);
    }
}
