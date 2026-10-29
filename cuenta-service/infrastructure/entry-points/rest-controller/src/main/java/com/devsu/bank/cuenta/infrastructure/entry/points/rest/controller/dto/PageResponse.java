package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.dto;

import java.util.List;

/** Offset page envelope (api-design.md §8.2). */
public record PageResponse<T>(List<T> items, PageMetadata page) {

  public record PageMetadata(int number, int size, long totalElements, long totalPages) {}
}
