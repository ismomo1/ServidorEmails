package net.javaguides.springboot.controller;

import net.javaguides.springboot.repository.EmailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import jakarta.transaction.*;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    private final EntityManager entityManager;

    @Autowired
    public EmailService(EmailRepository emailRepository, EntityManager entityManager) {
        this.emailRepository = emailRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public long getIndex() {
        Query query = entityManager.createNativeQuery("SELECT MAX(email_id) FROM emails");
        Long maxId = (Long) query.getSingleResult();

        // Manejo de valor nulo
        if (maxId == null) {
            return 0; // O cualquier valor por defecto
        }

        return maxId;
    }
}
