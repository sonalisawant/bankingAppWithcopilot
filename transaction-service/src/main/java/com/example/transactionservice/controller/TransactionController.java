package com.example.transactionservice.controller;

import com.example.shared.model.Transaction;
import com.example.shared.model.TransactionRequest;
import com.example.transactionservice.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.performTransaction(request));
    }
}
