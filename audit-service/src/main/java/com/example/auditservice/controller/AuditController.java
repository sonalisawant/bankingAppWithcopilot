package com.example.auditservice.controller;

import com.example.auditservice.repository.AuditRepository;
import com.example.shared.model.AuditEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAudits() {
        return ResponseEntity.ok(auditRepository.findAll());
    }
}
