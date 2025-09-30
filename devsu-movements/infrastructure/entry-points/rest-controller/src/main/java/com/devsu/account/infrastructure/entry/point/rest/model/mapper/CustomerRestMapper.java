package com.devsu.account.infrastructure.entry.point.rest.model.mapper;

import com.devsu.account.Customer;
import com.devsu.account.infrastructure.entry.point.rest.model.request.CreateCustomerRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.request.UpdateCustomerRequestDTO;
import com.devsu.account.infrastructure.entry.point.rest.model.response.CreatedCustomerResponseDTO;
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

    CreatedCustomerResponseDTO toResponse(Customer customer);

    List<CreatedCustomerResponseDTO> toResponse(List<Customer> customer);
}
