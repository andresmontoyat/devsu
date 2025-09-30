package com.devsu.account;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
    private UUID id;
    private Boolean active;
    private String name;
    private String genre;
    private Integer age;
    private String identification;
    private String address;
    private String phone;
    private String clientId;
    private String password;
}
