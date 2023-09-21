package net.javaguides.springboot.controller;

import net.javaguides.springboot.model.Email;
import net.javaguides.springboot.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    // Endpoint para crear un nuevo correo electrónico
    @PostMapping
    public Email createEmail(@RequestBody Email email) {
        if(email.getUpdateDate() == null) {
            email.setUpdateDate(Email.obtenerFechaHoraActual());
        }
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
}
