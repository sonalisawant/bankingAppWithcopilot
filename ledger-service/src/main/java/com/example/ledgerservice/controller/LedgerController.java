package com.example.ledgerservice.controller;

import com.example.ledgerservice.repository.LedgerRepository;
import com.example.shared.model.LedgerEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    private final LedgerRepository ledgerRepository;

    public LedgerController(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping
    public ResponseEntity<List<LedgerEntry>> getLedgerEntries() {
        return ResponseEntity.ok(ledgerRepository.findAll());
    }
}
