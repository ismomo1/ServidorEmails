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

    // Endpoint para obtener todos los correos electrónicos
    @GetMapping("/all")
    public ResponseEntity<EmailListDTO> getAllEmails() {
        List<Email> emails = emailRepository.findAll();

        List<EmailDTO> emailDTOs = emails.stream()
                .map(EmailDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new EmailListDTO(emailDTOs));
    }

    // Endpoint para obtener un correo por id
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

    // Endpoint para obtener uno o varios correos por id
    @GetMapping("")
    public ResponseEntity<EmailListDTO> getEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);

        List<Email> emails = emailRepository.findAllById(uniqueIds);

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = emails.stream()
                    .map(EmailDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new EmailListDTO(emailDTOs));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    // Endpoint para crear un nuevo correo electrónico
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

    @PostMapping
    public ResponseEntity<EmailListDTO> createEmails(@RequestBody EmailListDTO emailListDTO) {
        List<EmailDTO> createdEmails = new ArrayList<>();

        if (emailListDTO != null && emailListDTO.getEmails() != null) {
            for (EmailDTO emailDTO : emailListDTO.getEmails()) {
                Email email = new Email(emailDTO);
                if(email.getState() < 1 && email.getState() > 4) {
                    createdEmails.clear();
                    createdEmails.add(emailDTO);
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new EmailListDTO(createdEmails));
                }
                if(email.getEmailId() == null || emailRepository.findById(email.getEmailId()).isPresent()) {
                    createdEmails.clear();
                    createdEmails.add(emailDTO);
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                            .body(new EmailListDTO(createdEmails));
                }

                emailRepository.save(email);
                createdEmails.add(emailDTO);
            }
        }

        return ResponseEntity.ok(new EmailListDTO(createdEmails));
    }

    // Endpoint para marcar como eliminados uno o varios correos electrónicos
    @PutMapping("/deletedState")
    public ResponseEntity<EmailListDTO> setDeletedState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.ELIMINADO);
    }

    // Endpoint para marcar como enviado uno o varios correos en estado borrador
    @PutMapping("/sentState")
    public ResponseEntity<EmailListDTO> setSentState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.ENVIADO, EmailState.BORRADOR);
    }

    // Endpoint para marcar como spam uno o varios correos electrónicos
    @PutMapping("/spamState")
    public ResponseEntity<EmailListDTO> setSpamState(@RequestParam List<Long> ids) {
        return setEmailStateByID(ids, EmailState.SPAM);
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

//    // Endpoint para marcar como enviado un borrador de un correo electrónico
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

    // Endpoint para actualizar un correo electrónico existente (To,CC,Body)
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

    // Endpoint para eliminar un correo electrónico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long id) {
        emailRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para eliminar todos los correos
    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> deleteAllEmails() {
        emailRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
