package com.example.accountservice.service;

import com.example.accountservice.repository.AccountRepository;
import com.example.shared.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountService accountService;

    private Account alice;
    private Account bob;

    @BeforeEach
    void setUp() {
        alice = new Account("ACC-ALICE", "Alice", new BigDecimal("1000.00"));
        bob   = new Account("ACC-BOB",   "Bob",   new BigDecimal("500.00"));
    }

    // ─── CREATE / SAVE ───────────────────────────────────────────────────────

    @Test
    @DisplayName("save() persists a new account and returns it")
    void save_persistsNewAccount() {
        when(repository.save(alice)).thenReturn(alice);

        Account result = accountService.save(alice);

        assertThat(result.getAccountId()).isEqualTo("ACC-ALICE");
        assertThat(result.getOwner()).isEqualTo("Alice");
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
        verify(repository, times(1)).save(alice);
    }

    @Test
    @DisplayName("save() with zero balance is allowed")
    void save_withZeroBalance() {
        Account zero = new Account("ACC-NEW", "Charlie", BigDecimal.ZERO);
        when(repository.save(zero)).thenReturn(zero);

        Account result = accountService.save(zero);

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─── FIND ALL ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() returns all accounts")
    void findAll_returnsAllAccounts() {
        when(repository.findAll()).thenReturn(List.of(alice, bob));

        List<Account> accounts = accountService.findAll();

        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getOwner).containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("findAll() returns empty list when no accounts exist")
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(accountService.findAll()).isEmpty();
    }

    // ─── FIND BY ID ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns account when present")
    void findById_whenPresent_returnsAccount() {
        when(repository.findById("ACC-ALICE")).thenReturn(Optional.of(alice));

        Optional<Account> result = accountService.findById("ACC-ALICE");

        assertThat(result).isPresent();
        assertThat(result.get().getOwner()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findById() returns empty when not found")
    void findById_whenAbsent_returnsEmpty() {
        when(repository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThat(accountService.findById("UNKNOWN")).isEmpty();
    }

    // ─── DEBIT ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("debit() reduces balance correctly")
    void debit_reducesBalance() {
        when(repository.findById("ACC-ALICE")).thenReturn(Optional.of(alice));
        when(repository.save(alice)).thenReturn(alice);

        Account result = accountService.debit("ACC-ALICE", new BigDecimal("200.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("debit() exact balance leaves zero")
    void debit_exactBalance_leavesZero() {
        when(repository.findById("ACC-ALICE")).thenReturn(Optional.of(alice));
        when(repository.save(alice)).thenReturn(alice);

        Account result = accountService.debit("ACC-ALICE", new BigDecimal("1000.00"));

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("debit() throws when balance is insufficient")
    void debit_insufficientBalance_throwsException() {
        when(repository.findById("ACC-BOB")).thenReturn(Optional.of(bob));

        assertThatThrownBy(() -> accountService.debit("ACC-BOB", new BigDecimal("600.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("debit() throws when account does not exist")
    void debit_accountNotFound_throwsException() {
        when(repository.findById("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.debit("MISSING", new BigDecimal("50.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found");
    }

    // ─── CREDIT ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("credit() increases balance correctly")
    void credit_increasesBalance() {
        when(repository.findById("ACC-BOB")).thenReturn(Optional.of(bob));
        when(repository.save(bob)).thenReturn(bob);

        Account result = accountService.credit("ACC-BOB", new BigDecimal("250.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("credit() throws when account does not exist")
    void credit_accountNotFound_throwsException() {
        when(repository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.credit("GHOST", new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    @DisplayName("credit() with zero amount keeps balance unchanged")
    void credit_withZeroAmount_balanceUnchanged() {
        when(repository.findById("ACC-ALICE")).thenReturn(Optional.of(alice));
        when(repository.save(alice)).thenReturn(alice);

        Account result = accountService.credit("ACC-ALICE", BigDecimal.ZERO);

        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
    }
}
