package com.example.accountservice.config;

import com.example.accountservice.service.AccountService;
import com.example.shared.model.Account;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer {

    private final AccountService accountService;

    public DataInitializer(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostConstruct
    public void init() {
        accountService.save(new Account("A1001", "Alice", BigDecimal.valueOf(1500)));
        accountService.save(new Account("A1002", "Bob", BigDecimal.valueOf(900)));
        accountService.save(new Account("A1003", "Charlie", BigDecimal.valueOf(2000)));
        accountService.save(new Account("A1004", "Diana", BigDecimal.valueOf(3200)));
        accountService.save(new Account("A1005", "Ethan", BigDecimal.valueOf(1250)));
        accountService.save(new Account("A1006", "Fiona", BigDecimal.valueOf(450)));
    }
}
