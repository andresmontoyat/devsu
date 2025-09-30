package com.devsu.customer.infrastructure.entry.point.rest.model.response;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponseDTO {
    private UUID id;

    private String name;

    private String genre;

    private Integer age;

    private String identification;

    private String address;

    private String phone;

    private Boolean active;
}
