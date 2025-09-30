package com.devsu.account.usecase;

import com.devsu.account.Account;
import com.devsu.account.Movement;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import com.devsu.account.usecase.port.outbound.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementProcessUseCaseTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private MovementProcessUseCase useCase;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
    }

    @Test
    void process_ShouldPersistMovementAndUpdateAccountBalance_WhenAccountExists_PositiveValue() {
        // Arrange
        Account account = Account.builder()
                .id(accountId)
                .initialBalance(new BigDecimal("100.00"))
                .build();

        Movement movement = Movement.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .movementDate(LocalDateTime.now())
                .value(new BigDecimal("50.00"))
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.update(eq(accountId), any(Account.class))).thenAnswer(inv -> Optional.of(inv.getArgument(1)));

        // Act
        useCase.process(movement);

        // Assert
        // Movement should be saved with starting balance (100.00)
        verify(movementRepository).save(argThat(m -> new BigDecimal("100.00").compareTo(m.getBalance()) == 0));

        // Account should be updated with new balance 150.00
        verify(accountRepository).update(eq(accountId), accountCaptor.capture());
        assertThat(accountCaptor.getValue().getInitialBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void process_ShouldThrow_WhenAccountNotFound() {
        // Arrange
        Movement movement = Movement.builder()
                .accountId(accountId)
                .value(new BigDecimal("25.00"))
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> useCase.process(movement))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found with id");

        verifyNoInteractions(movementRepository);
        verify(accountRepository, never()).update(any(), any());
    }

    @Test
    void process_ShouldDecreaseBalance_WhenNegativeMovementValue() {
        // Arrange
        Account account = Account.builder()
                .id(accountId)
                .initialBalance(new BigDecimal("100.00"))
                .build();

        Movement movement = Movement.builder()
                .accountId(accountId)
                .value(new BigDecimal("-30.00"))
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.update(eq(accountId), any(Account.class))).thenAnswer(inv -> Optional.of(inv.getArgument(1)));

        // Act
        useCase.process(movement);

        // Assert
        verify(accountRepository).update(eq(accountId), accountCaptor.capture());
        assertThat(accountCaptor.getValue().getInitialBalance()).isEqualByComparingTo("70.00");
    }

    @Test
    void process_ShouldNotChangeBalance_WhenZeroMovementValue() {
        // Arrange
        Account account = Account.builder()
                .id(accountId)
                .initialBalance(new BigDecimal("100.00"))
                .build();

        Movement movement = Movement.builder()
                .accountId(accountId)
                .value(BigDecimal.ZERO)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.update(eq(accountId), any(Account.class))).thenAnswer(inv -> Optional.of(inv.getArgument(1)));

        // Act
        useCase.process(movement);

        // Assert
        verify(accountRepository).update(eq(accountId), accountCaptor.capture());
        assertThat(accountCaptor.getValue().getInitialBalance()).isEqualByComparingTo("100.00");
    }
}
