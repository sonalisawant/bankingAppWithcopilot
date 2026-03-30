package com.example.accountservice.controller;

import com.example.accountservice.service.AccountService;
import com.example.shared.model.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        return ResponseEntity.ok(accountService.save(account));
    }

    @GetMapping
    public ResponseEntity<List<Account>> listAccounts() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        return accountService.findById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{accountId}/debit")
    public ResponseEntity<Account> debit(@PathVariable String accountId, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        return ResponseEntity.ok(accountService.debit(accountId, amount));
    }

    @PostMapping("/{accountId}/credit")
    public ResponseEntity<Account> credit(@PathVariable String accountId, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        return ResponseEntity.ok(accountService.credit(accountId, amount));
    }
}
