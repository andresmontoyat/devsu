package com.devsu.account.usecase.port.outbound;

import com.devsu.account.Movement;

public interface MovementPublisher {
    void movementReceived(Movement movement);
}
