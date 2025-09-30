package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Movement;

/**
 * This interface represents the use case for receiving a movement.
 */
public interface ReceiveMovementUseCasePort {
    /**
     *
     * @param movement
     */
    void receive(Movement movement);
}
