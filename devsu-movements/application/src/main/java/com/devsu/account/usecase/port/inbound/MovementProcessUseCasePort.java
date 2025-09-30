package com.devsu.account.usecase.port.inbound;

import com.devsu.account.Movement;

public interface MovementProcessUseCasePort {
    void process(Movement movement);
}
