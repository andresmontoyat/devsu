package com.devsu.account.infrastructure.entry.point.rest;

import com.devsu.account.Movement;
import com.devsu.account.infrastructure.entry.point.rest.model.mapper.MovementRestMapper;
import com.devsu.account.infrastructure.entry.point.rest.model.request.ReceiveMovementRequestDTO;
import com.devsu.account.usecase.port.inbound.ReceiveMovementUseCasePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movements")
public class MovementController {

    private final ReceiveMovementUseCasePort receiveMovementUseCasePort;
    private final MovementRestMapper movementRestMapper;

    @PostMapping
    public ResponseEntity<Void> receiveMovement(@Validated @RequestBody ReceiveMovementRequestDTO request) {
        log.info("Receiving movement: {}", request);
        Movement movement = movementRestMapper.toDomain(request);
        receiveMovementUseCasePort.receive(movement);
        log.info("Movement received successfully: {}", movement);
        return ResponseEntity.accepted().build();
    }
}
