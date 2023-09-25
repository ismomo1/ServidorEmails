package net.javaguides.springboot.repository;

import net.javaguides.springboot.model.EmailListDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "email-service-client", url = "http://localhost:8080")
public interface EmailServiceClient {
    @GetMapping("/emails/all")
    EmailListDTO getAllEmails();
}
