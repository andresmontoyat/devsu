package com.devsu.customer.infrastructure.entry.point.rest.model.mapper;

import com.devsu.customer.Customer;
import com.devsu.customer.infrastructure.entry.point.rest.model.request.CreateCustomerRequestDTO;
import com.devsu.customer.infrastructure.entry.point.rest.model.request.UpdateCustomerRequestDTO;
import com.devsu.customer.infrastructure.entry.point.rest.model.response.CustomerResponseDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerRestMapper {

    Customer toDomain(CreateCustomerRequestDTO request);

    Customer toDomain(UpdateCustomerRequestDTO request);

    CustomerResponseDTO toResponse(Customer customer);

    List<CustomerResponseDTO> toResponse(List<Customer> customer);
}
