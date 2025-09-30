package com.devsu.account.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.devsu.account.Account;
import com.devsu.account.usecase.port.outbound.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountUseCase useCase;

    private Account buildAccount(UUID id) {
        return Account.builder()
                .id(id)
                .accountNumber(1234567890L)
                .accountType("SAVINGS")
                .initialBalance(new BigDecimal("100.00"))
                .status(null) // will be set by create()
                .customerId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("create() should set status true and return saved account")
    void create_setsStatusTrue_andPersists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Account toCreate = buildAccount(null);
        Account persisted = buildAccount(id);
        persisted.setStatus(Boolean.TRUE);

        when(accountRepository.save(any(Account.class))).thenReturn(persisted);

        // Act
        Account result = useCase.create(toCreate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isTrue();
        Mockito.verify(accountRepository).save(any(Account.class));
    }

    @Nested
    class GetById {
        @Test
        @DisplayName("getById() should return account when present")
        void getById_found() {
            UUID id = UUID.randomUUID();
            Account acc = buildAccount(id);
            when(accountRepository.findById(id)).thenReturn(Optional.of(acc));

            Account result = useCase.getById(id);

            assertThat(result).isEqualTo(acc);
        }

        @Test
        @DisplayName("getById() should throw when not found with expected message")
        void getById_notFound() {
            UUID id = UUID.randomUUID();
            when(accountRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getById(id))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Account not found with id: " + id);
        }
    }

    @Test
    @DisplayName("getAll() should delegate to repository and return list")
    void getAll_delegates() {
        Account a1 = buildAccount(UUID.randomUUID());
        Account a2 = buildAccount(UUID.randomUUID());
        when(accountRepository.getAll()).thenReturn(List.of(a1, a2));

        List<Account> result = useCase.getAll();

        assertThat(result).containsExactly(a1, a2);
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("update() should return updated account when present")
        void update_found() {
            UUID id = UUID.randomUUID();
            Account updates = buildAccount(null);
            Account updated = buildAccount(id);
            when(accountRepository.update(eq(id), eq(updates))).thenReturn(Optional.of(updated));

            Account result = useCase.update(id, updates);
            assertThat(result).isEqualTo(updated);
        }

        @Test
        @DisplayName("update() should throw when account not found")
        void update_notFound() {
            UUID id = UUID.randomUUID();
            Account updates = buildAccount(null);
            when(accountRepository.update(eq(id), eq(updates))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.update(id, updates))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Account not found with id: " + id);
        }
    }

    @Nested
    class ChangeStatusTests {
        @Test
        @DisplayName("changeStatus() should return updated account when present")
        void changeStatus_found() {
            UUID id = UUID.randomUUID();
            Account updated = buildAccount(id);
            updated.setStatus(Boolean.FALSE);
            when(accountRepository.changeStatus(id, false)).thenReturn(Optional.of(updated));

            Account result = useCase.changeStatus(id, false);
            assertThat(result).isEqualTo(updated);
        }

        @Test
        @DisplayName("changeStatus() should throw when account not found (current message says Customer)")
        void changeStatus_notFound() {
            UUID id = UUID.randomUUID();
            when(accountRepository.changeStatus(id, true)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.changeStatus(id, true))
                    .isInstanceOf(NoSuchElementException.class)
                    // Note: message currently says Customer, matching production code
                    .hasMessage("Customer not found with id: " + id);
        }
    }
}
