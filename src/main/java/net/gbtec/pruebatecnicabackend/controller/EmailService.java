package net.gbtec.pruebatecnicabackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gbtec.pruebatecnicabackend.model.Email;
import net.gbtec.pruebatecnicabackend.model.EmailDTO;
import net.gbtec.pruebatecnicabackend.model.EmailListDTO;
import net.gbtec.pruebatecnicabackend.model.EmailState;
import net.gbtec.pruebatecnicabackend.rabbitMQ.MessageProducer;
import net.gbtec.pruebatecnicabackend.repository.EmailRepository;

import net.gbtec.pruebatecnicabackend.repository.EmailServiceClient;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import jakarta.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    private final EntityManager entityManager;
    private final EmailServiceClient emailServiceClient;
    private final MessageProducer messageProducer;
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    public EmailService(EmailRepository emailRepository, EntityManager entityManager, EmailServiceClient emailServiceClient, MessageProducer messageProducer) {
        this.emailRepository = emailRepository;
        this.entityManager = entityManager;
        this.emailServiceClient = emailServiceClient;
        this.messageProducer = messageProducer;
    }

    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    @Scheduled(cron = "0 0 10 * * *") // Ejecutar todos los días a las 10:00 am
    public void markCarlEmailsAsSpam() {
        List<Email> carlEmails = emailRepository.findByEmailFrom("carl@gbtec.es");
        List<Email> carlSpamEmails = carlEmails.stream().peek(email -> email.setState(EmailState.SPAM.getValue())).toList();
        emailRepository.saveAll(carlSpamEmails);
    }

    @Transactional
    public long getNextIndex() {
        Query query = entityManager.createNativeQuery("SELECT MAX(email_id) FROM emails");
        Long maxId = (Long) query.getSingleResult();

        if (maxId == null) {
            return 0;
        }

        return maxId + 1;
    }

    public static Timestamp parseTimestampFromString(String timestampString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date parsedDate = dateFormat.parse(timestampString);
        return new Timestamp(parsedDate.getTime());
    }

    public List<EmailDTO> getEmailDTOS(List<Email> emails) {
        List<EmailDTO> emailDTOs = emails.stream()
                .map(EmailDTO::new)
                .collect(Collectors.toList());
        return emailDTOs;
    }

    public ResponseEntity<EmailListDTO> getEmailListDTOResponse(List<Email> emails) {
        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = getEmailDTOS(emails);
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<EmailListDTO> setEmailStateByID(List<Long> ids, EmailState setEmailState) {
        Set<Long> uniqueIds = new HashSet<>(ids);

        List<Email> emails = emailRepository.findAllById(uniqueIds);

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = emails.stream()
                    .peek(email -> email.setState(setEmailState.getValue()))
                    .map(EmailDTO::new)
                    .collect(Collectors.toList());
            emailRepository.saveAll(emails);
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<EmailListDTO> setEmailStateByID(List<Long> ids, EmailState setEmailState, EmailState filterEmailState) {
        Set<Long> uniqueIds = new HashSet<>(ids);

        List<Email> emails = emailRepository.findAllById(uniqueIds);
        emails = emails.stream().filter(email -> email.getState() == filterEmailState.getValue()).collect(Collectors.toList());

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = emails.stream()
                    .peek(email -> email.setState(setEmailState.getValue()))
                    .map(EmailDTO::new)
                    .collect(Collectors.toList());
            emailRepository.saveAll(emails);
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public String convertToString(EmailListDTO emailListDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonString = objectMapper.writeValueAsString(emailListDTO);

            return jsonString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String convertToString(Email email) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonString = objectMapper.writeValueAsString(email);

            return jsonString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void rabbitMQmessage(String message) {
        try {
            messageProducer.sendMessage("\n" + message);
        } catch (AmqpException ex) {
            log.warn("Error al interactuar con RabbitMQ: " + ex.getMessage());
        }
    }
}
