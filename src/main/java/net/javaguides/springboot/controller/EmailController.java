package net.javaguides.springboot.controller;

import net.javaguides.springboot.model.Email;
import net.javaguides.springboot.model.EmailDTO;
import net.javaguides.springboot.model.EmailListDTO;
import net.javaguides.springboot.model.EmailState;
import net.javaguides.springboot.rabbitMQ.MessageProducer;
import net.javaguides.springboot.repository.EmailRepository;
import net.javaguides.springboot.repository.EmailServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        messageProducer.sendMessage("\ngetEmails: " + ids + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para obtener todos los emails
    @GetMapping("/all")
    public ResponseEntity<EmailListDTO> getAllEmails() {
        List<Email> emails = emailRepository.findAll();
        List<EmailDTO> emailDTOs = emailService.getEmailDTOS(emails);
        EmailListDTO emailListDTO = new EmailListDTO(emailDTOs);
        messageProducer.sendMessage("\ngetAllEmails\n" + emailService.convertToString(emailListDTO));
        return ResponseEntity.ok(emailListDTO);
//        EmailListDTO emailListDTO = emailServiceClient.getAllEmails();
//        return ResponseEntity.ok(emailListDTO);
    }

    // Endpoint para filtrar por emailFrom
    @GetMapping("/findByEmailFrom")
    public ResponseEntity<EmailListDTO> getEmailsByEmailFrom(@RequestParam String emailFrom) {
        List<Email> emails = emailRepository.findByEmailFrom(emailFrom);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        messageProducer.sendMessage("\ngetEmailsByEmailFrom: " + emailFrom + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para filtrar por un email contenido en emailTo
    @GetMapping("/findByEmailTo")
    public ResponseEntity<EmailListDTO> getEmailsByEmailTo(
            @RequestParam String emailTo) {
        List<Email> emails = emailRepository.findByEmailToContaining(emailTo);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        messageProducer.sendMessage("\ngetEmailsByEmailTo: " + emailTo + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para filtrar por un email contenido en emailCC
    @GetMapping("/findByEmailCC")
    public ResponseEntity<EmailListDTO> getEmailsByEmailCC(@RequestParam String emailCC) {
        List<Email> emails = emailRepository.findByEmailCCContaining(emailCC);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);
        messageProducer.sendMessage("\ngetEmailsByEmailCC: " + emailCC + "\n" + emailService.convertToString(response.getBody()));
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
        messageProducer.sendMessage("\ngetEmailsByState: " + EmailState.fromValue(state).name() + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para obtener todos los emails de un rango de fechas de última actualización
    @GetMapping("/findByDateRange")
    public ResponseEntity<EmailListDTO> getEmailsByDateRange(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        Timestamp startDateT = emailService.parseTimestampFromString(startDate);
        Timestamp endDateT = emailService.parseTimestampFromString(endDate);

        List<Email> emails = emailRepository.findByUpdateDateBetween(startDateT, endDateT);
        ResponseEntity<EmailListDTO> response = emailService.getEmailListDTOResponse(emails);

        messageProducer.sendMessage("\ngetEmailsByDateRange: startDate " + startDateT + " - endDate " + endDateT + "\n" + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para crear uno o varios nuevos emails
    @PostMapping
    public ResponseEntity<EmailListDTO> createEmails(@RequestBody EmailListDTO emailListDTO) {
        messageProducer.sendMessage("\ncreateEmails:\n");
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
            messageProducer.sendMessage("Solicitud incorrecta, revisa los datos del cuerpo de la solicitud");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

//        emailRepository.saveAll(createdEmails);

        if (!createdEmailsDTO.isEmpty()) {
            messageProducer.sendMessage("\nEmails registrados:\n" + emailService.convertToString(new EmailListDTO(createdEmailsDTO))
                    + "\nEmails no registrados por:\n-ID no válido (ya existente o sin indicar):\n" + emailService.convertToString(new EmailListDTO(notValidIdEmailsDTO))
                    + "\n-Estado distinto de 1(Enviado), 2(Borrador), 3(Eliminado), 4(Spam)\n" + emailService.convertToString(new EmailListDTO(notValidStateEmailsDTO))
                    + "\n-Falta emailFrom\n" + emailService.convertToString(new EmailListDTO(notValidFromEmailsDTO)));
            return ResponseEntity.ok(new EmailListDTO(createdEmailsDTO));
        } else {
            messageProducer.sendMessage("\nNinguno de los emails indicados es válido. Emails no registrados por:"
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
        messageProducer.sendMessage("\nsetDeletedState\nCorreos marcados como eliminados:\n"
                + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para marcar como enviado uno o varios emails en estado borrador
    @PutMapping("/sentState")
    public ResponseEntity<EmailListDTO> setSentState(@RequestParam List<Long> ids) {
        ResponseEntity<EmailListDTO> response = emailService.setEmailStateByID(ids, EmailState.ENVIADO, EmailState.BORRADOR);
        messageProducer.sendMessage("\nsetSentState\nCorreos marcados como enviados:\n"
                + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para marcar como spam uno o varios emails
    @PutMapping("/spamState")
    public ResponseEntity<EmailListDTO> setSpamState(@RequestParam List<Long> ids) {
        ResponseEntity<EmailListDTO> response = emailService.setEmailStateByID(ids, EmailState.SPAM);
        messageProducer.sendMessage("\nsetSpamState\nCorreos marcados como spam:\n"
                + emailService.convertToString(response.getBody()));
        return response;
    }

    // Endpoint para actualizar un borrador de un email existente (To,CC,Body)
    @PutMapping("/{id}")
    public ResponseEntity<EmailDTO> updateEmail(@PathVariable Long id, @RequestBody EmailDTO updatedEmailDTO) {
        messageProducer.sendMessage("\nupdateEmail: " + id);
        Optional<Email> emailOptional = emailRepository.findById(id);

        if (!emailOptional.isPresent()) {
            messageProducer.sendMessage("\nNo existe ningún correo con el id " + id);
            return ResponseEntity.notFound().build();
        }

        Email existingEmail = emailOptional.get();
        if (existingEmail.getState() != EmailState.BORRADOR.getValue()) {
            messageProducer.sendMessage("\nEl correo con el id " + id + " no se encuentra en el estado " + EmailState.BORRADOR.name());
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
                messageProducer.sendMessage("\nEl correo con el id " + id + " no ha sido modificado debido a que ninguno de los campos emailTo, emailCC o emailBody indicados modificaba los datos actuales.");
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
            messageProducer.sendMessage("\nCorreo con el id " + id + " modificado correctamente con los datos indicados:\n" + emailService.convertToString(savedEmail));
            return ResponseEntity.ok(new EmailDTO(savedEmail));
        } else {
            messageProducer.sendMessage("\nSolicitud incorrecta, revisa los datos del cuerpo de la solicitud. Faltan los campos emailTo, emailCC y emailBody.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EmailDTO(existingEmail));
        }
    }

    // Endpoint para eliminar uno o varios emails de base de datos
    @DeleteMapping("")
    public ResponseEntity<EmailListDTO> deleteEmails(@RequestParam List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        messageProducer.sendMessage("\ndeleteEmails: " + uniqueIds);

        List<Email> emails = emailRepository.findAllById(uniqueIds);

        if (!emails.isEmpty()) {
            List<EmailDTO> emailDTOs = emailService.getEmailDTOS(emails);
            emailRepository.deleteAllById(ids);
            ResponseEntity<EmailListDTO> response = ResponseEntity.ok(new EmailListDTO(emailDTOs));
            messageProducer.sendMessage("\nSe han eliminado los correos existentes para los ids " + uniqueIds + "\n" + emailService.convertToString(response.getBody()));
            return response;
        } else {
            messageProducer.sendMessage("\nNo existen correos con alguno de los ids " + uniqueIds);
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para eliminar todos los emails de base de datos
    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> deleteAllEmails() {
        emailRepository.deleteAll();
        messageProducer.sendMessage("\ndeleteAllEmails\nSe han borrado todos los emails de la base de datos." );
        return ResponseEntity.noContent().build();
    }
}
