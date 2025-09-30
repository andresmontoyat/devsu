package com.devsu.account.usecase.port.outbound;

import com.devsu.account.pagination.PageRequest;
import com.devsu.account.pagination.PageResult;
import com.devsu.account.report.AccountStatusReportItem;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountReportRepository {
    PageResult<AccountStatusReportItem> findAccountsByPeriodAndClient(
            LocalDate fromDate, LocalDate toDate, UUID clientId, PageRequest pageRequest);
}
