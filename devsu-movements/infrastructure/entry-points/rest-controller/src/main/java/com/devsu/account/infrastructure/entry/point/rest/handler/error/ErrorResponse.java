package com.devsu.account.infrastructure.entry.point.rest.handler.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;

    /** HTTP status code (e.g., 400, 404, 500) */
    private int status;

    /** HTTP reason phrase (e.g., "Bad Request", "Not Found") */
    private String error;

    /** General message about the error */
    private String message;

    /** URI of the request that caused the error */
    private String path;

    /** Optional list of detailed validation errors (e.g., field: message) */
    private List<String> fieldErrors;
}
