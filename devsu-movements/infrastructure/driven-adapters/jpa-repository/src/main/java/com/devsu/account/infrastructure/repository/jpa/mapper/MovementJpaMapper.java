package com.devsu.account.infrastructure.repository.jpa.mapper;

import com.devsu.account.Movement;
import com.devsu.account.infrastructure.repository.jpa.entity.MovementEntity;
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
public interface MovementJpaMapper {
    MovementEntity toEntity(Movement movement);

    void toEntity(Movement movement, @MappingTarget MovementEntity entity);

    Movement toDomain(MovementEntity entity);
}
