package com.devsu.customer.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "client_id", name = "uk_customers_client_id"),
        })
@PrimaryKeyJoinColumn(name = "id")
public class CustomerEntity extends PersonEntity {

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "active", nullable = false)
    private Boolean active;

    public CustomerEntity() {
        super();
    }
}
