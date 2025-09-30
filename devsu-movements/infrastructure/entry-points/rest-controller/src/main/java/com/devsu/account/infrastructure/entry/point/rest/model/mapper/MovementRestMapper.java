package com.devsu.account.infrastructure.entry.point.rest.model.mapper;

import com.devsu.account.Movement;
import com.devsu.account.infrastructure.entry.point.rest.model.request.ReceiveMovementRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MovementRestMapper {

    Movement toDomain(ReceiveMovementRequestDTO request);
}
