package net.javaguides.springboot.controller;

import net.javaguides.springboot.model.Email;
import net.javaguides.springboot.model.EmailDTO;
import net.javaguides.springboot.model.RecipientDTO;
import net.javaguides.springboot.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emails")
public class EmailController {
    private final EmailRepository emailRepository;

    @Autowired
    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    // Endpoint para obtener todos los correos electrónicos
    @GetMapping
    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    // Endpoint para obtener un correo por id
    @GetMapping("/{id}")
    public ResponseEntity<EmailDTO> getEmail(@PathVariable Long id) {
        Optional<Email> emailOptional = emailRepository.findById(id);

        if (emailOptional.isPresent()) {
            Email email = emailOptional.get();
            EmailDTO emailDTO = new EmailDTO(email);
            return ResponseEntity.ok(emailDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para crear un nuevo correo electrónico
    @PostMapping
    public Email createEmail(@RequestBody EmailDTO emailDTO) {
        Email email = new Email(emailDTO);

        return emailRepository.save(email);
    }

    // Endpoint para actualizar un correo electrónico existente
    @PutMapping("/{id}")
    public ResponseEntity<Email> updateEmail(@PathVariable Long id, @RequestBody Email updatedEmail) {
        Optional<Email> emailOptional = emailRepository.findById(id);

        if (!emailOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        updatedEmail.setEmailId(id);
        Email savedEmail = emailRepository.save(updatedEmail);
        return ResponseEntity.ok(savedEmail);
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
