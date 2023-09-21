package net.javaguides.springboot.controller;

import net.javaguides.springboot.repository.EmailRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final EmailRepository emailRepository;

    public HelloController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @GetMapping("/")
    public String sayNothing() {
        return "Hello, Nobody!";
    }
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
