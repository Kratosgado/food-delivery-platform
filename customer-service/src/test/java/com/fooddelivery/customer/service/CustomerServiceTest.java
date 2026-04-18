package com.fooddelivery.customer.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fooddelivery.customer.dto.*;
import com.fooddelivery.customer.dto.CustomerDtos.UpdateCustomerDto;
import com.fooddelivery.customer.exception.DuplicateResourceException;
import com.fooddelivery.customer.model.Customer;
import com.fooddelivery.customer.repository.CustomerRepository;
import com.fooddelivery.customer.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;

  @InjectMocks private CustomerService customerService;

  private static final Long CUSTOMER_ID = 1L;
  private static final String EMAIL = "john@example.com";
  private static final String PASSWORD = "password123";
  private static final String ENCODED_PASSWORD = "encoded_password";
  private static final String USERNAME = "johndoe";
  private static final String JWT_TOKEN = "jwt_token_123";

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("should login successfully with valid credentials")
  void testLoginSuccess() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .email(EMAIL)
            .password(ENCODED_PASSWORD)
            .username(USERNAME)
            .firstName("John")
            .lastName("Doe")
            .role(Customer.Role.CUSTOMER)
            .build();

    LoginDto loginDto = new LoginDto(EMAIL, PASSWORD);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
    when(jwtUtil.generateToken(customer)).thenReturn(JWT_TOKEN);

    AuthResponseDto result = customerService.login(loginDto);

    assertThat(result).isNotNull();
    assertThat(result.token()).isEqualTo(JWT_TOKEN);
    assertThat(result.tokenType()).isEqualTo("Bearer");
    assertThat(result.customer()).isNotNull();
    assertThat(result.customer().id()).isEqualTo(CUSTOMER_ID);
    assertThat(result.customer().email()).isEqualTo(EMAIL);

    verify(customerRepository).findByEmail(EMAIL);
    verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
    verify(jwtUtil).generateToken(customer);
  }

  @Test
  @DisplayName("should throw exception when customer not found during login")
  void testLoginCustomerNotFound() {
    LoginDto loginDto = new LoginDto(EMAIL, PASSWORD);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.login(loginDto))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Customer not found");
  }

  @Test
  @DisplayName("should throw exception when password is incorrect")
  void testLoginInvalidPassword() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .email(EMAIL)
            .password(ENCODED_PASSWORD)
            .build();

    LoginDto loginDto = new LoginDto(EMAIL, PASSWORD);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

    assertThatThrownBy(() -> customerService.login(loginDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid credentials");
  }

  @Test
  @DisplayName("should register customer successfully")
  void testRegisterSuccess() {
    CustomerRegistrationDto registrationDto =
        new CustomerRegistrationDto(
            USERNAME, "john@example.com", "John", "Doe", PASSWORD, "1234567890", "123 Main St", "New York");

    when(customerRepository.existsByUsername(USERNAME)).thenReturn(false);
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
    when(jwtUtil.generateToken(any(Customer.class))).thenReturn(JWT_TOKEN);
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(
            invocation -> {
              Customer c = invocation.getArgument(0);
              c.setId(CUSTOMER_ID);
              return c;
            });

    AuthResponseDto result = customerService.register(registrationDto);

    assertThat(result).isNotNull();
    assertThat(result.token()).isEqualTo(JWT_TOKEN);
    assertThat(result.tokenType()).isEqualTo("Bearer");
    assertThat(result.customer()).isNotNull();
    assertThat(result.customer().username()).isEqualTo(USERNAME);

    verify(customerRepository).existsByUsername(USERNAME);
    verify(customerRepository).existsByEmail(EMAIL);
    verify(passwordEncoder).encode(PASSWORD);
    verify(customerRepository).save(any(Customer.class));
    verify(jwtUtil).generateToken(any(Customer.class));
  }

  @Test
  @DisplayName("should throw exception when username already exists")
  void testRegisterDuplicateUsername() {
    CustomerRegistrationDto registrationDto =
        new CustomerRegistrationDto(
            USERNAME, "john@example.com", "John", "Doe", PASSWORD, "1234567890", "123 Main St", "New York");

    when(customerRepository.existsByUsername(USERNAME)).thenReturn(true);

    assertThatThrownBy(() -> customerService.register(registrationDto))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Username already taken");

    verify(customerRepository, never()).save(any());
  }

  @Test
  @DisplayName("should throw exception when email already exists")
  void testRegisterDuplicateEmail() {
    CustomerRegistrationDto registrationDto =
        new CustomerRegistrationDto(
            USERNAME, EMAIL, "John", "Doe", PASSWORD, "1234567890", "123 Main St", "New York");

    when(customerRepository.existsByUsername(USERNAME)).thenReturn(false);
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertThatThrownBy(() -> customerService.register(registrationDto))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Email already registered");

    verify(customerRepository, never()).save(any());
  }

  @Test
  @DisplayName("should retrieve customer by id")
  void testGetByIdSuccess() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .username(USERNAME)
            .email(EMAIL)
            .firstName("John")
            .lastName("Doe")
            .role(Customer.Role.CUSTOMER)
            .build();

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

    CustomerResponseDto result = customerService.getById(CUSTOMER_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(CUSTOMER_ID);
    assertThat(result.email()).isEqualTo(EMAIL);
    verify(customerRepository).findById(CUSTOMER_ID);
  }

  @Test
  @DisplayName("should throw exception when customer not found by id")
  void testGetByIdNotFound() {
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.getById(CUSTOMER_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Customer not found");
  }

  @Test
  @DisplayName("should retrieve customer summary")
  void testGetSummary() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .firstName("John")
            .lastName("Doe")
            .email(EMAIL)
            .deliveryAddress("123 Main St")
            .build();

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

    CustomerSummaryDto result = customerService.getSummary(CUSTOMER_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(CUSTOMER_ID);
    assertThat(result.fullName()).isEqualTo("John Doe");
    assertThat(result.email()).isEqualTo(EMAIL);
    assertThat(result.deliveryAddress()).isEqualTo("123 Main St");
  }

  @Test
  @DisplayName("should update customer details")
  void testUpdateCustomerSuccess() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .deliveryAddress("123 Main St")
            .city("New York")
            .build();

    UpdateCustomerDto updateDto =
        new UpdateCustomerDto("Jane", "Smith", "9876543210", "456 Elm St", "Boston");

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(customerRepository.save(customer))
        .thenAnswer(
            invocation -> {
              Customer c = invocation.getArgument(0);
              return c;
            });

    CustomerResponseDto result = customerService.update(CUSTOMER_ID, updateDto);

    assertThat(result).isNotNull();
    assertThat(result.firstName()).isEqualTo("Jane");
    assertThat(result.lastName()).isEqualTo("Smith");
    assertThat(result.phone()).isEqualTo("9876543210");
    assertThat(result.deliveryAddress()).isEqualTo("456 Elm St");
    assertThat(result.city()).isEqualTo("Boston");

    verify(customerRepository).save(customer);
  }

  @Test
  @DisplayName("should update only provided fields")
  void testUpdatePartialCustomerDetails() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .deliveryAddress("123 Main St")
            .city("New York")
            .build();

    UpdateCustomerDto updateDto = new UpdateCustomerDto("Jane", null, null, null, null);

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(customerRepository.save(customer))
        .thenAnswer(
            invocation -> {
              Customer c = invocation.getArgument(0);
              return c;
            });

    CustomerResponseDto result = customerService.update(CUSTOMER_ID, updateDto);

    assertThat(result.firstName()).isEqualTo("Jane");
    assertThat(result.lastName()).isEqualTo("Doe");
    assertThat(result.phone()).isEqualTo("1234567890");
  }

  @Test
  @DisplayName("should promote customer to restaurant owner")
  void testMakeRestaurantOwner() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .role(Customer.Role.CUSTOMER)
            .build();

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

    customerService.makeRestaurantOwner(CUSTOMER_ID);

    assertThat(customer.getRole()).isEqualTo(Customer.Role.RESTAURANT_OWNER);
    verify(customerRepository).save(customer);
  }

  @Test
  @DisplayName("should not change role if already restaurant owner")
  void testMakeRestaurantOwnerAlreadyOwner() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .role(Customer.Role.RESTAURANT_OWNER)
            .build();

    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

    customerService.makeRestaurantOwner(CUSTOMER_ID);

    assertThat(customer.getRole()).isEqualTo(Customer.Role.RESTAURANT_OWNER);
    verify(customerRepository, never()).save(any());
  }

  @Test
  @DisplayName("should find customer by email")
  void testFindByEmailSuccess() {
    Customer customer =
        Customer.builder()
            .id(CUSTOMER_ID)
            .email(EMAIL)
            .build();

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    Customer result = customerService.findByEmail(EMAIL);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
    assertThat(result.getEmail()).isEqualTo(EMAIL);
    verify(customerRepository).findByEmail(EMAIL);
  }

  @Test
  @DisplayName("should throw exception when customer not found by email")
  void testFindByEmailNotFound() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.findByEmail(EMAIL))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Customer not found");
  }
}
