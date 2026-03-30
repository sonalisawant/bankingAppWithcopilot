package com.example.auditservice.service;

import com.example.shared.model.AuditEvent;
import com.example.shared.model.Transaction;
import com.example.auditservice.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditListener.class);
    private final AuditRepository auditRepository;

    public AuditListener(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @KafkaListener(topics = "${banking.topics.transaction}", groupId = "audit-service-group")
    public void onTransactionEvent(Transaction transaction) {
        AuditEvent event = new AuditEvent(
                "TRANSACTION",
                String.format("Transaction %s from %s to %s amount %s status %s",
                        transaction.getId(), transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount(), transaction.getStatus()),
                Instant.now()
        );
        auditRepository.save(event);
        logger.info("Saved audit event for transaction {}", transaction.getId());
    }
}
