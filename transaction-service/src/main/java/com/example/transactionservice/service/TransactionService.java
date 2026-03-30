package com.example.transactionservice.service;

import com.example.shared.model.Account;
import com.example.shared.model.Transaction;
import com.example.shared.model.TransactionRequest;
import com.example.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final String transactionTopic;
    private final String accountServiceUrl;

    public TransactionService(TransactionRepository transactionRepository,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              RestTemplate restTemplate,
                              @Value("${banking.topics.transaction}") String transactionTopic,
                              @Value("${accounts.service.url}") String accountServiceUrl) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
        this.transactionTopic = transactionTopic;
        this.accountServiceUrl = accountServiceUrl;
    }

    @Transactional
    public Transaction performTransaction(TransactionRequest request) {
        logger.info("Processing transaction from {} to {} for {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", request.getAmount());

        restTemplate.postForObject(accountServiceUrl + "/api/accounts/{id}/debit", payload, Account.class, request.getFromAccountId());
        restTemplate.postForObject(accountServiceUrl + "/api/accounts/{id}/credit", payload, Account.class, request.getToAccountId());

        Transaction transaction = new Transaction(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                Instant.now(),
                "COMPLETED"
        );
        transaction = transactionRepository.save(transaction);
        kafkaTemplate.send(transactionTopic, transaction);
        return transaction;
    }
}
