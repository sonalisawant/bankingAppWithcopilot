package com.example.accountservice.service;

import com.example.accountservice.repository.AccountRepository;
import com.example.shared.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public List<Account> findAll() {
        return repository.findAll();
    }

    public Optional<Account> findById(String accountId) {
        return repository.findById(accountId);
    }

    @Transactional
    public Account save(Account account) {
        return repository.save(account);
    }

    @Transactional
    public Account debit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance for account " + accountId);
        }
        account.setBalance(account.getBalance().subtract(amount));
        return repository.save(account);
    }

    @Transactional
    public Account credit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        account.setBalance(account.getBalance().add(amount));
        return repository.save(account);
    }
}
