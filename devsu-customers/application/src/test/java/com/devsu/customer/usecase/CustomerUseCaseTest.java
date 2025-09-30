package com.devsu.customer.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.devsu.customer.Customer;
import com.devsu.customer.usecase.port.outbound.CustomerPublisher;
import com.devsu.customer.usecase.port.outbound.CustomerRepository;
import com.devsu.customer.usecase.port.outbound.SecurityUtil;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerPublisher customerPublisher;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private CustomerUseCase useCase;

    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
    }

    @Test
    @DisplayName("create() should set active=true, hash password, save and publish event")
    void create_shouldSaveWithHashedPasswordAndPublish() {
        var customer = Customer.builder().name("John").password("plain").build();

        when(securityUtil.hashPassword("plain")).thenReturn("hashed");

        var saved = Customer.builder()
                .id(UUID.randomUUID())
                .name("John")
                .password("hashed")
                .active(true)
                .build();
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        var result = useCase.create(customer);

        // verify that password was hashed and active set before saving
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        var toSave = captor.getValue();
        assertThat(toSave.getActive()).isTrue();
        assertThat(toSave.getPassword()).isEqualTo("hashed");

        // returned value
        assertThat(result).isEqualTo(saved);

        // publisher invoked
        verify(customerPublisher).customerCreated(saved);
        verifyNoMoreInteractions(customerPublisher);
    }

    @Test
    @DisplayName("delete() should delete by id and publish event")
    void delete_shouldDeleteAndPublish() {
        doNothing().when(customerRepository).delete(id);

        useCase.delete(id);

        verify(customerRepository).delete(id);
        verify(customerPublisher).customerDeleted(id);
        verifyNoMoreInteractions(customerPublisher);
    }

    @Test
    @DisplayName("getById() should return customer when found")
    void getById_shouldReturnCustomer() {
        var found = Customer.builder().id(id).name("Jane").build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(found));

        var result = useCase.getById(id);

        assertThat(result).isEqualTo(found);
        verify(customerRepository).findById(id);
    }

    @Test
    @DisplayName("getById() should throw when not found")
    void getById_shouldThrowWhenNotFound() {
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(id))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Customer not found with id: " + id);
    }

    @Test
    @DisplayName("getAll() should return list from repository")
    void getAll_shouldReturnList() {
        var list = List.of(
                Customer.builder().id(UUID.randomUUID()).name("A").build(),
                Customer.builder().id(UUID.randomUUID()).name("B").build());
        when(customerRepository.getAll()).thenReturn(list);

        var result = useCase.getAll();

        assertThat(result).isEqualTo(list);
        verify(customerRepository).getAll();
    }

    @Test
    @DisplayName("update() should return updated entity when found")
    void update_shouldReturnUpdated() {
        var toUpdate = Customer.builder().name("New").build();
        var updated = Customer.builder().id(id).name("New").build();
        when(customerRepository.update(id, toUpdate)).thenReturn(Optional.of(updated));

        var result = useCase.update(id, toUpdate);

        assertThat(result).isEqualTo(updated);
        verify(customerRepository).update(id, toUpdate);
    }

    @Test
    @DisplayName("update() should throw when customer not found")
    void update_shouldThrowWhenNotFound() {
        var toUpdate = Customer.builder().name("New").build();
        when(customerRepository.update(id, toUpdate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(id, toUpdate))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Customer not found with id: " + id);
    }

    @Test
    @DisplayName("changeStatus() should update active flag, publish event and return updated")
    void changeStatus_shouldPublishAndReturnUpdated() {
        var updated = Customer.builder().id(id).active(false).build();
        when(customerRepository.changeStatus(id, false)).thenReturn(Optional.of(updated));

        var result = useCase.changeStatus(id, false);

        assertThat(result).isEqualTo(updated);
        verify(customerRepository).changeStatus(id, false);
        verify(customerPublisher).customerStatusChanged(id, false);
    }

    @Test
    @DisplayName("changeStatus() should throw when customer not found")
    void changeStatus_shouldThrowWhenNotFound() {
        when(customerRepository.changeStatus(id, false)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.changeStatus(id, false))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Customer not found with id: " + id);
    }
}
