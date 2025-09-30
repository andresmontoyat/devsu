package com.devsu.account.infrastructure.entry.point.rest.model.response;

import com.devsu.account.enums.CustomerStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatedCustomerResponseDTO {
    private UUID id;

    private String name;

    private String genre;

    private Integer age;

    private String identification;

    private String address;

    private String phone;

    private CustomerStatus status;
}
