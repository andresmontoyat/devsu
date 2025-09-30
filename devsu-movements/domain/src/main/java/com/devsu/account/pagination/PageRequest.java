package com.devsu.account.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private int page;
    private int size;
    private String sortField;
    private SortDirection sortDirection;

    public enum SortDirection {
        ASC,
        DESC
    }
}
