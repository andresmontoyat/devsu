package com.devsu.account.infrastructure.entry.point.rest.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateCustomerRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String genre;

    @NotNull
    @Positive
    private Integer age;

    @NotBlank
    private String identification;

    @NotBlank
    private String address;

    @NotBlank
    private String phone;

    @NotBlank
    private String clientId;

    @NotBlank
    private String password;
}
