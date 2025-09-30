package com.devsu.account.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devsu.account.Account;
import com.devsu.account.Customer;
import com.devsu.account.Movement;
import com.devsu.account.enums.CustomerStatus;
import com.devsu.account.usecase.exception.InsufficientInitialBalanceException;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import com.devsu.account.usecase.port.outbound.CustomerRepository;
import com.devsu.account.usecase.port.outbound.MovementPublisher;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovementReceiveUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementPublisher movementPublisher;

    @InjectMocks
    private MovementReceiveUseCase useCase;

    private Movement buildMovement(UUID customerId, UUID accountId) {
        return Movement.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .accountId(accountId)
                .value(new BigDecimal("50.00"))
                .balance(new BigDecimal("150.00"))
                .build();
    }

    private Customer activeCustomer(UUID id) {
        return Customer.builder()
                .id(id)
                .status(CustomerStatus.ACTIVE)
                .name("John Doe")
                .build();
    }

    private Customer inactiveCustomer(UUID id) {
        return Customer.builder()
                .id(id)
                .status(CustomerStatus.INACTIVE)
                .name("Jane Doe")
                .build();
    }

    private Account account(UUID id, boolean active, BigDecimal initialBalance) {
        return Account.builder()
                .id(id)
                .accountNumber(1234567890L)
                .accountType("SAVINGS")
                .initialBalance(initialBalance)
                .status(active)
                .customerId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("receive() should publish movement when validations pass")
    void receive_success_publishesMovement() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Movement movement = buildMovement(customerId, accountId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer(customerId)));
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account(accountId, true, new BigDecimal("100.00"))));

        useCase.receive(movement);

        verify(movementPublisher).movementReceived(movement);
    }

    @Test
    @DisplayName("receive() should throw when customer not found")
    void receive_customerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Movement movement = buildMovement(customerId, accountId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.receive(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found with id: " + customerId);

        verify(movementPublisher, never()).movementReceived(any());
    }

    @Test
    @DisplayName("receive() should throw when customer is not active")
    void receive_inactiveCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Movement movement = buildMovement(customerId, accountId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(inactiveCustomer(customerId)));

        assertThatThrownBy(() -> useCase.receive(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer is not active. Movement cannot be processed.");

        verify(movementPublisher, never()).movementReceived(any());
    }

    @Test
    @DisplayName("receive() should throw when account not found")
    void receive_accountNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Movement movement = buildMovement(customerId, accountId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer(customerId)));
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.receive(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account not found with id: " + accountId);

        verify(movementPublisher, never()).movementReceived(any());
    }

    @Test
    @DisplayName("receive() should throw when account is not active")
    void receive_inactiveAccount() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Movement movement = buildMovement(customerId, accountId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer(customerId)));
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account(accountId, false, new BigDecimal("100.00"))));

        assertThatThrownBy(() -> useCase.receive(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account is not active. Movement cannot be processed.");

        verify(movementPublisher, never()).movementReceived(any());
    }

    @Nested
    class InitialBalanceValidation {
        @Test
        @DisplayName("receive() should throw when initial balance is null")
        void receive_initialBalanceNull() {
            UUID customerId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Movement movement = buildMovement(customerId, accountId);

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer(customerId)));
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account(accountId, true, null)));

            assertThatThrownBy(() -> useCase.receive(movement))
                    .isInstanceOf(InsufficientInitialBalanceException.class)
                    .hasMessage("Account has zero initial balance. Movement cannot be processed.");

            verify(movementPublisher, never()).movementReceived(any());
        }

        @Test
        @DisplayName("receive() should throw when initial balance is zero")
        void receive_initialBalanceZero() {
            UUID customerId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            Movement movement = buildMovement(customerId, accountId);

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer(customerId)));
            when(accountRepository.findById(accountId))
                    .thenReturn(Optional.of(account(accountId, true, BigDecimal.ZERO)));

            assertThatThrownBy(() -> useCase.receive(movement))
                    .isInstanceOf(InsufficientInitialBalanceException.class)
                    .hasMessage("Account has zero initial balance. Movement cannot be processed.");

            verify(movementPublisher, never()).movementReceived(any());
        }
    }
}
