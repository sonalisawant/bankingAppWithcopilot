package com.example.auditservice.repository;

import com.example.shared.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEvent, Long> {
}
