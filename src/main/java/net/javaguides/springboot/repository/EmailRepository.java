package net.javaguides.springboot.repository;

import net.javaguides.springboot.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long>{
}
