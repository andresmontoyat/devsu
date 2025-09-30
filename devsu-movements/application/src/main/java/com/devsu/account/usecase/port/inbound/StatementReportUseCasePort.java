package com.devsu.account.usecase.port.inbound;

import com.devsu.account.pagination.PageRequest;
import com.devsu.account.pagination.PageResult;
import com.devsu.account.report.AccountStatusReportItem;
import java.time.LocalDate;
import java.util.UUID;

public interface StatementReportUseCasePort {
    PageResult<AccountStatusReportItem> getReport(
            LocalDate fromDate, LocalDate toDate, UUID clientId, PageRequest pageRequest);
}
