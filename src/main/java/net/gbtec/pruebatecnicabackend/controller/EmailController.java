package net.gbtec.pruebatecnicabackend.controller;

import net.gbtec.pruebatecnicabackend.model.EmailDTO;
import net.gbtec.pruebatecnicabackend.rabbitMQ.MessageProducer;
import net.gbtec.pruebatecnicabackend.repository.EmailServiceClient;
import net.gbtec.pruebatecnicabackend.model.Email;
import net.gbtec.pruebatecnicabackend.model.EmailListDTO;
import net.gbtec.pruebatecnicabackend.model.EmailState;
import net.gbtec.pruebatecnicabackend.repository.EmailRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("/emails")
public class EmailController {
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final EmailServiceClient emailServiceClient;
    private final MessageProducer messageProducer;
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);


    @Autowired
    public EmailController(EmailRepository emailRepository, EmailService emailService, EmailServiceClient emailServiceClient, MessageProducer messageProducer) {
        this.emailRepository = emailRepository;
        this.emailService = emailService;
        this.emailServiceClient = emailServiceClient;
        this.messageProducer = messageProducer;
    }

    // Endpoint para obtener uno o varios emails por id
    @GetMapping("")
    public ResponseEntity<EmailListDTO> getEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        List<Email> emails = emailRepository.findAllById(uniqueIds);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        emailService.rabbitMQmessage("getEmails: " + ids + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para obtener todos los emails
    @GetMapping("/all")
    public ResponseEntity<EmailListDTO> getAllEmails() {
        List<Email> emails = emailRepository.findAll();
        List<EmailDTO> emailDTOs = emailService.getEmailDTOS(emails);
        EmailListDTO emailListDTO = new EmailListDTO(emailDTOs);
        emailService.rabbitMQmessage("getAllEmails\n" + emailService.convertToString(emailListDTO));
        return ResponseEntity.ok(emailListDTO);
//        EmailListDTO emailListDTO = emailServiceClient.getAllEmails();
//        return ResponseEntity.ok(emailListDTO);
    }

    // Endpoint para filtrar por emailFrom
    @GetMapping("/findByEmailFrom")
    public ResponseEntity<EmailListDTO> getEmailsByEmailFrom(@RequestParam String emailFrom) {
        List<Email> emails = emailRepository.findByEmailFrom(emailFrom);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        emailService.rabbitMQmessage("getEmailsByEmailFrom: " + emailFrom + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para filtrar por un email contenido en emailTo
    @GetMapping("/findByEmailTo")
    public ResponseEntity<EmailListDTO> getEmailsByEmailTo(
            @RequestParam String emailTo) {
        List<Email> emails = emailRepository.findByEmailToContaining(emailTo);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        emailService.rabbitMQmessage("getEmailsByEmailTo: " + emailTo + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para filtrar por un email contenido en emailCC
    @GetMapping("/findByEmailCC")
    public ResponseEntity<EmailListDTO> getEmailsByEmailCC(@RequestParam String emailCC) {
        List<Email> emails = emailRepository.findByEmailCCContaining(emailCC);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        emailService.rabbitMQmessage("getEmailsByEmailCC: " + emailCC + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para filtrar por estado
    @GetMapping("/findByState")
    public ResponseEntity<EmailListDTO> getEmailsByState(@RequestParam int state) {
        List<Email> emails = emailRepository.findByState(state);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        if (state < 1 || state > 4) {
            state = 0;
        }
        emailService.rabbitMQmessage("getEmailsByState: " + EmailState.fromValue(state).name() + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para obtener todos los emails de un rango de fechas de última actualización
    @GetMapping("/findByDateRange")
    public ResponseEntity<EmailListDTO> getEmailsByDateRange(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        Timestamp startDateT = emailService.parseTimestampFromString(startDate);
        Timestamp endDateT = emailService.parseTimestampFromString(endDate);

        List<Email> emails = emailRepository.findByUpdateDateBetween(startDateT, endDateT);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);

        emailService.rabbitMQmessage("getEmailsByDateRange: startDate " + startDateT + " - endDate " + endDateT + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para crear uno o varios nuevos emails
    @PostMapping
    public ResponseEntity<EmailListDTO> createEmails(@RequestBody EmailListDTO emailListDTO) {
        emailService.rabbitMQmessage("createEmails:\n");
        List<EmailDTO> createdEmailsDTO = new ArrayList<>();
        List<EmailDTO> notValidEmailsDTO = new ArrayList<>();
        List<EmailDTO> notValidStateEmailsDTO = new ArrayList<>();
        List<EmailDTO> notValidIdEmailsDTO = new ArrayList<>();
        List<EmailDTO> notValidFromEmailsDTO = new ArrayList<>();

        if (emailListDTO != null && emailListDTO.getEmails() != null) {
            for (EmailDTO emailDTO : emailListDTO.getEmails()) {
                Email email = new Email(emailDTO);
                if(email.getState() < 1 || email.getState() > 4) {
                    notValidEmailsDTO.add(emailDTO);
                    notValidStateEmailsDTO.add(emailDTO);
                    continue;
                }
                if(email.getEmailId() == null || emailRepository.findById(email.getEmailId()).isPresent()) {
                    notValidEmailsDTO.add(emailDTO);
                    notValidIdEmailsDTO.add(emailDTO);
                    continue;
                }

                if(emailDTO.getEmailFrom() == null) {
                    notValidEmailsDTO.add(emailDTO);
                    notValidFromEmailsDTO.add(emailDTO);
                    continue;
                }

                emailRepository.save(email);
                createdEmailsDTO.add(emailDTO);
            }
        } else {
            emailService.rabbitMQmessage("Solicitud incorrecta, revisa los datos del cuerpo de la solicitud");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

//        emailRepository.saveAll(createdEmails);

        if (!createdEmailsDTO.isEmpty()) {
            emailService.rabbitMQmessage("Emails registrados:\n" + emailService.convertToString(new EmailListDTO(createdEmailsDTO))
                            + "\nEmails no registrados por:\n-ID no válido (ya existente o sin indicar):\n" + emailService.convertToString(new EmailListDTO(notValidIdEmailsDTO))
                            + "\n-Estado distinto de 1(Enviado), 2(Borrador), 3(Eliminado), 4(Spam)\n" + emailService.convertToString(new EmailListDTO(notValidStateEmailsDTO))
                            + "\n-Falta emailFrom\n" + emailService.convertToString(new EmailListDTO(notValidFromEmailsDTO)));
            return ResponseEntity.ok(new EmailListDTO(createdEmailsDTO));
        } else {
            emailService.rabbitMQmessage("Ninguno de los emails indicados es válido. Emails no registrados por:"
                            + "\n-ID no válido (ya existente o sin indicar):\n" + emailService.convertToString(new EmailListDTO(notValidIdEmailsDTO))
                            + "\n-Estado distinto de 1(Enviado), 2(Borrador), 3(Eliminado), 4(Spam)\n" + emailService.convertToString(new EmailListDTO(notValidStateEmailsDTO)));
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new EmailListDTO(notValidEmailsDTO));
        }
    }

    // Endpoint para marcar como eliminados uno o varios emails
    @PutMapping("/deletedState")
    public ResponseEntity<EmailListDTO> setDeletedState(@RequestParam List<Long> ids) {
        ResponseEntity<EmailListDTO> response = emailService.setEmailStateByID(ids, EmailState.ELIMINADO);
        emailService.rabbitMQmessage("setDeletedState\nCorreos marcados como eliminados:\n"
                + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para marcar como enviado uno o varios emails en estado borrador
    @PutMapping("/sentState")
    public ResponseEntity<EmailListDTO> setSentState(@RequestParam List<Long> ids) {
        ResponseEntity<EmailListDTO> response = emailService.setEmailStateByID(ids, EmailState.ENVIADO, EmailState.BORRADOR);
        emailService.rabbitMQmessage("setSentState\nCorreos marcados como enviados:\n"
                        + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para marcar como spam uno o varios emails
    @PutMapping("/spamState")
    public ResponseEntity<EmailListDTO> setSpamState(@RequestParam List<Long> ids) {
        ResponseEntity<EmailListDTO> response = emailService.setEmailStateByID(ids, EmailState.SPAM);
        emailService.rabbitMQmessage("setSpamState\nCorreos marcados como spam:\n"
                + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para actualizar un borrador de un email existente (To,CC,Body)
    @PutMapping("/{id}")
    public ResponseEntity<EmailDTO> updateEmail(@PathVariable Long id, @RequestBody EmailDTO updatedEmailDTO) {
        emailService.rabbitMQmessage("updateEmail: " + id + "\n");
        Optional<Email> emailOptional = emailRepository.findById(id);

        if (!emailOptional.isPresent()) {
            emailService.rabbitMQmessage("No existe ningún correo con el id " + id);
            return ResponseEntity.notFound().build();
        }

        Email existingEmail = emailOptional.get();
        if (existingEmail.getState() != EmailState.BORRADOR.getValue()) {
            emailService.rabbitMQmessage("El correo con el id " + id + " no se encuentra en el estado " + EmailState.BORRADOR.name());
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
                emailService.rabbitMQmessage("El correo con el id " + id + " no ha sido modificado debido a que ninguno de los campos emailTo, emailCC o emailBody indicados modificaba los datos actuales.");
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
            emailService.rabbitMQmessage("Correo con el id " + id + " modificado correctamente con los datos indicados:\n" + emailService.convertToString(savedEmail));
            return ResponseEntity.ok(new EmailDTO(savedEmail));
        } else {
            emailService.rabbitMQmessage("\nSolicitud incorrecta, revisa los datos del cuerpo de la solicitud. Faltan los campos emailTo, emailCC y emailBody.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EmailDTO(existingEmail));
        }
    }

    // Endpoint para eliminar uno o varios emails de base de datos
    @DeleteMapping("")
    public ResponseEntity<EmailListDTO> deleteEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        emailService.rabbitMQmessage("deleteEmails: " + uniqueIds);

        List<Email> emails = emailRepository.findAllById(uniqueIds);

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = emailService.getEmailDTOS(emails);
            emailRepository.deleteAllById(ids);
            ResponseEntity<EmailListDTO> response = ResponseEntity.ok(new EmailListDTO(emailDTOs));
            emailService.rabbitMQmessage("Se han eliminado los correos existentes para los ids " + uniqueIds + "\n" + emailService.convertToString(response.getBody()));
            return response;
        } else {
            emailService.rabbitMQmessage("No existen correos con alguno de los ids " + uniqueIds);
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para eliminar todos los emails de base de datos
    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> deleteAllEmails() {
        emailRepository.deleteAll();
        emailService.rabbitMQmessage("deleteAllEmails\nSe han borrado todos los emails de la base de datos." );
        return ResponseEntity.noContent().build();
    }
}
