package com.example.ledgerservice.repository;

import com.example.shared.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
}
