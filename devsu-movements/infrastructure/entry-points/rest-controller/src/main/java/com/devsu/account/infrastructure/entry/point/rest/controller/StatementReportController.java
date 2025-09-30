package com.devsu.account.infrastructure.entry.point.rest.controller;

import com.devsu.account.pagination.PageRequest;
import com.devsu.account.pagination.PageResult;
import com.devsu.account.report.AccountStatusReportItem;
import com.devsu.account.usecase.port.inbound.StatementReportUseCasePort;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class StatementReportController {

    private final StatementReportUseCasePort statementReportUseCasePort;

    @GetMapping
    public ResponseEntity<PageResult<AccountStatusReportItem>> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "accountNumber,asc") String sort) {
        log.info(
                "Fetching account status report: from={}, to={}, clientId={}, page={}, size={}, sort={}",
                fromDate,
                toDate,
                clientId,
                page,
                size,
                sort);

        String[] sortParts = sort.split(",");
        PageRequest.SortDirection direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                ? PageRequest.SortDirection.DESC
                : PageRequest.SortDirection.ASC;
        String sortField = sortParts[0];

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortField(sortField)
                .sortDirection(direction)
                .build();

        PageResult<AccountStatusReportItem> result =
                statementReportUseCasePort.getReport(fromDate, toDate, clientId, pageRequest);
        return ResponseEntity.ok(result);
    }
}
