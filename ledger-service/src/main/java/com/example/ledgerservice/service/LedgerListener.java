package com.example.ledgerservice.service;

import com.example.ledgerservice.repository.LedgerRepository;
import com.example.shared.model.LedgerEntry;
import com.example.shared.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LedgerListener {

    private static final Logger logger = LoggerFactory.getLogger(LedgerListener.class);
    private final LedgerRepository ledgerRepository;

    public LedgerListener(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @KafkaListener(topics = "${banking.topics.transaction}", groupId = "ledger-service-group")
    public void consumeTransaction(Transaction transaction) {
        ledgerRepository.save(new LedgerEntry(
                transaction.getFromAccountId(),
                transaction.getAmount().negate(),
                "DEBIT",
                Instant.now(),
                "Debit for transaction " + transaction.getId()
        ));
        ledgerRepository.save(new LedgerEntry(
                transaction.getToAccountId(),
                transaction.getAmount(),
                "CREDIT",
                Instant.now(),
                "Credit for transaction " + transaction.getId()
        ));
        logger.info("Ledger entries created for transaction {}", transaction.getId());
    }
}
