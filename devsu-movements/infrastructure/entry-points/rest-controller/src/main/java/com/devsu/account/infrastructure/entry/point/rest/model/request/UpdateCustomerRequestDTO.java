package com.devsu.account.infrastructure.entry.point.rest.model.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCustomerRequestDTO {

    private String name;

    private String genre;

    @Positive
    private Integer age;

    private String address;

    private String phone;
}
