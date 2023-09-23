package net.javaguides.springboot.controller;

import net.javaguides.springboot.model.Email;
import net.javaguides.springboot.repository.EmailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import jakarta.transaction.*;

import java.util.List;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static Timestamp parseTimestampFromString(String timestampString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date parsedDate = dateFormat.parse(timestampString);
        return new Timestamp(parsedDate.getTime());
    }
}
