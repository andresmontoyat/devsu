package com.devsu.customer.infrastructure.repository.jpa.mapper;

import com.devsu.customer.Customer;
import com.devsu.customer.infrastructure.repository.jpa.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerJpaMapper {
    CustomerEntity toEntity(Customer customer);

    void toEntity(Customer customer, @MappingTarget CustomerEntity entity);

    Customer toDomain(CustomerEntity entity);
}
