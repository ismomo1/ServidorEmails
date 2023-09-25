package net.gbtec.pruebatecnicabackend.repository;

import net.gbtec.pruebatecnicabackend.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long>{
    List<Email> findByEmailFrom(String emailFrom);
    @Query(value = "SELECT * FROM emails WHERE email_to LIKE %:email%", nativeQuery = true)
    List<Email> findByEmailToContaining(@Param("email") String emailTo);
    @Query(value = "SELECT * FROM emails WHERE email_cc LIKE %:email%", nativeQuery = true)
    List<Email> findByEmailCCContaining(@Param("email") String emailCC);
    List<Email> findByState(int state);
    List<Email> findByUpdateDateBetween(Timestamp startDate, Timestamp endDate);
}
