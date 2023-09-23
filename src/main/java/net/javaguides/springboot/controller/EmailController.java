package net.javaguides.springboot.controller;

import net.javaguides.springboot.model.Email;
import net.javaguides.springboot.model.EmailDTO;
import net.javaguides.springboot.model.EmailListDTO;
import net.javaguides.springboot.model.EmailState;
import net.javaguides.springboot.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emails")
public class EmailController {
    private final EmailRepository emailRepository;
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailRepository emailRepository, EmailService emailService) {
        this.emailRepository = emailRepository;
        this.emailService = emailService;
    }

    // Endpoint para obtener uno o varios emails por id
    @GetMapping("")
    public ResponseEntity<EmailListDTO> getEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        List<Email> emails = emailRepository.findAllById(uniqueIds);
        return getEmailListDTO(emails);
    }

    // Endpoint para obtener todos los emails
    @GetMapping("/all")
    public ResponseEntity<EmailListDTO> getAllEmails() {
        List<Email> emails = emailRepository.findAll();
        List<EmailDTO> emailDTOs = getEmailDTOS(emails);
        return ResponseEntity.ok(new EmailListDTO(emailDTOs));
    }

    // Endpoint para filtrar por emailFrom
    @GetMapping("/findByEmailFrom")
    public ResponseEntity<EmailListDTO> getEmailsByEmailFrom(@RequestParam String emailFrom) {
        List<Email> emails = emailRepository.findByEmailFrom(emailFrom);
        return getEmailListDTO(emails);
    }

    // Endpoint para filtrar por un email contenido en emailTo
    @GetMapping("/findByEmailTo")
    public ResponseEntity<EmailListDTO> getEmailsByEmailTo(
            @RequestParam String emailTo) {
        List<Email> emails = emailRepository.findByEmailToContaining(emailTo);
        return getEmailListDTO(emails);
    }

    // Endpoint para filtrar por un email contenido en emailCC
    @GetMapping("/findByEmailCC")
    public ResponseEntity<EmailListDTO> getEmailsByEmailCC(@RequestParam String emailCC) {
        List<Email> emails = emailRepository.findByEmailCCContaining(emailCC);
        return getEmailListDTO(emails);
    }

    // Endpoint para filtrar por estado
    @GetMapping("/findByState")
    public ResponseEntity<EmailListDTO> getEmailsByState(@RequestParam int state) {
        List<Email> emails = emailRepository.findByState(state);
        return getEmailListDTO(emails);
    }

    // Endpoint para obtener todos los emails de un rango de fechas
    @GetMapping("/findByDateRange")
    public ResponseEntity<EmailListDTO> getEmailsByDateRange(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        Timestamp startDateT = emailService.parseTimestampFromString(startDate);
        Timestamp endDateT = emailService.parseTimestampFromString(endDate);

        List<Email> emails = emailRepository.findByUpdateDateBetween(startDateT, endDateT);

        return getEmailListDTO(emails);
    }

    // Endpoint para obtener un email por id
//    @GetMapping("/{id}")
//    public ResponseEntity<EmailDTO> getEmail(@PathVariable Long id) {
//        Optional<Email> emailOptional = emailRepository.findById(id);
//
//        if (emailOptional.isPresent()) {
//            Email email = emailOptional.get();
//            EmailDTO emailDTO = new EmailDTO(email);
//            return ResponseEntity.ok(emailDTO);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    // Endpoint para crear un nuevo email
//    @PostMapping
//    public ResponseEntity<EmailDTO> createEmail(@RequestBody EmailDTO emailDTO) {
//        Email email = new Email(emailDTO);
//        //email.setEmailFrom("default_user@gbtec.es");
//        if(email.getState() < 1 && email.getState() > 4) {
//            //email.setState(EmailState.BORRADOR.getValue());
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
//                    .body(emailDTO);
//        }
//        //email.setEmailId(emailService.getIndex() + 1);
//        if(email.getEmailId() == null || emailRepository.findById(email.getEmailId()).isPresent()) {
//            //email.setEmailId(emailService.getIndex() + 1);
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
//                    .body(emailDTO);
//        }
//
//        emailRepository.save(email);
//        return ResponseEntity.ok(emailDTO);
//    }

    // Endpoint para crear uno o varios nuevos emails
    @PostMapping
    public ResponseEntity<EmailListDTO> createEmails(@RequestBody EmailListDTO emailListDTO) {
        List<EmailDTO> createdEmailsDTO = new ArrayList<>();
        List<EmailDTO> notValidEmailsDTO = new ArrayList<>();
        List<Email> createdEmails = new ArrayList<>();

        if (emailListDTO != null && emailListDTO.getEmails() != null) {
            for (EmailDTO emailDTO : emailListDTO.getEmails()) {
                Email email = new Email(emailDTO);
                if(email.getState() < 1 && email.getState() > 4) {
                    notValidEmailsDTO.add(emailDTO);
                    continue;
                }
                if(email.getEmailId() == null || emailRepository.findById(email.getEmailId()).isPresent()) {
                    notValidEmailsDTO.add(emailDTO);
                    continue;
                }

                emailRepository.save(email);
                createdEmailsDTO.add(emailDTO);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

//        emailRepository.saveAll(createdEmails);

        if (notValidEmailsDTO.isEmpty()) {
            return ResponseEntity.ok(new EmailListDTO(createdEmailsDTO));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new EmailListDTO(notValidEmailsDTO));
        }
    }

    // Endpoint para marcar como eliminados uno o varios emails
    @PutMapping("/deletedState")
    public ResponseEntity<EmailListDTO> setDeletedState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.ELIMINADO);
    }

    // Endpoint para marcar como enviado uno o varios emails en estado borrador
    @PutMapping("/sentState")
    public ResponseEntity<EmailListDTO> setSentState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.ENVIADO, EmailState.BORRADOR);
    }

    // Endpoint para marcar como spam uno o varios emails
    @PutMapping("/spamState")
    public ResponseEntity<EmailListDTO> setSpamState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.SPAM);
    }

//    // Endpoint para marcar como enviado un borrador de un email
//    @PutMapping("send/{id}")
//    public ResponseEntity<EmailDTO> sendEmail(@PathVariable Long id) {
//        Optional<Email> emailOptional = emailRepository.findById(id);
//
//        if (!emailOptional.isPresent()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Email existingEmail = emailOptional.get();
//
//        if (existingEmail.getState() != EmailState.BORRADOR.getValue()) {
//            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
//                    .body(new EmailDTO(existingEmail));
//        }
//
//        existingEmail.setState(EmailState.ENVIADO.getValue());
//        return ResponseEntity.ok(new EmailDTO(emailRepository.save(existingEmail)));
//    }

    // Endpoint para actualizar un borrador de un email existente (To,CC,Body)
    @PutMapping("/{id}")
    public ResponseEntity<EmailDTO> updateEmail(@PathVariable Long id, @RequestBody EmailDTO updatedEmailDTO) {
        Optional<Email> emailOptional = emailRepository.findById(id);

        if (!emailOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Email existingEmail = emailOptional.get();
        if (existingEmail.getState() != EmailState.BORRADOR.getValue()) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(new EmailDTO(existingEmail));
        }

        Email updatedEmail = new Email(updatedEmailDTO);

        if (/*updatedEmailDTO.getEmailFrom() != null || */updatedEmailDTO.getEmailTo() != null || updatedEmailDTO.getEmailCC() != null || updatedEmailDTO.getEmailBody() != null) {

            if (
                    //(Objects.equals(updatedEmail.getEmailFrom(), existingEmail.getEmailFrom()) || updatedEmailDTO.getEmailFrom() == null) &&
                    (Objects.equals(updatedEmail.getEmailTo(), existingEmail.getEmailTo()) || updatedEmailDTO.getEmailTo() == null) &&
                    (Objects.equals(updatedEmail.getEmailCC(), existingEmail.getEmailCC()) || updatedEmailDTO.getEmailCC() == null) &&
                    (Objects.equals(updatedEmail.getEmailBody(), existingEmail.getEmailBody()) || updatedEmailDTO.getEmailBody() == null)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .body(new EmailDTO(existingEmail));
            }

//            if (updatedEmailDTO.getEmailFrom() == null) {
//                updatedEmail.setEmailFrom(existingEmail.getEmailFrom());
//            }

            if (updatedEmailDTO.getEmailTo() == null) {
                updatedEmail.setEmailTo(existingEmail.getEmailTo());
            }

            if (updatedEmailDTO.getEmailCC() == null) {
                updatedEmail.setEmailCC(existingEmail.getEmailCC());
            }

            if (updatedEmailDTO.getEmailBody() == null) {
                updatedEmail.setEmailBody(existingEmail.getEmailBody());
            }

            updatedEmail.setEmailId(id);
            updatedEmail.setState(existingEmail.getState());
            updatedEmail.setEmailFrom(existingEmail.getEmailFrom());
            Email savedEmail = emailRepository.save(updatedEmail);
            return ResponseEntity.ok(new EmailDTO(savedEmail));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EmailDTO(existingEmail));
        }
    }

//    // Endpoint para eliminar un email de base de datos
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteEmail(@PathVariable Long id) {
//        emailRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }

    // Endpoint para eliminar uno o varios emails de base de datos
    @DeleteMapping("")
    public ResponseEntity<EmailListDTO> deleteEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);

        List<Email> emails = emailRepository.findAllById(uniqueIds);

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = getEmailDTOS(emails);
            emailRepository.deleteAllById(ids);
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para eliminar todos los emails de base de datos
    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> deleteAllEmails() {
        emailRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private static List<EmailDTO> getEmailDTOS(List<Email> emails) {
        List<EmailDTO> emailDTOs = emails.stream()
                .map(EmailDTO::new)
                .collect(Collectors.toList());
        return emailDTOs;
    }

    private static ResponseEntity<EmailListDTO> getEmailListDTO(List<Email> emails) {
        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = getEmailDTOS(emails);
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<EmailListDTO> setEmailStateByID(List<Long> ids, EmailState setEmailState) {
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

    private ResponseEntity<EmailListDTO> setEmailStateByID(List<Long> ids, EmailState setEmailState, EmailState filterEmailState) {
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
}
