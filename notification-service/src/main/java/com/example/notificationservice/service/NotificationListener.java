package com.example.notificationservice.service;

import com.example.shared.model.NotificationEvent;
import com.example.shared.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(topics = "${banking.topics.transaction}", groupId = "notification-service-group")
    public void handleTransaction(Transaction transaction) {
        NotificationEvent notification = new NotificationEvent(
                transaction.getFromAccountId(),
                String.format("Transaction %s completed from %s to %s for %s.",
                        transaction.getId(), transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount())
        );
        logger.info("Notification produced: {}", notification.getMessage());
    }
}
