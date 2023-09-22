package net.javaguides.springboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="emails")
public class Email {
    @Id
    @Column(name = "email_id")
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;
    @Column(name = "email_from")
    private String emailFrom;
    @Column(name = "email_to")
    private String emailTo;
    @Column(name = "email_cc")
    private String emailCC;
    @Column(name = "email_body")
    private String emailBody;
    @Column(name = "email_state")
    @JsonProperty("state")
    private int state;
//    @ManyToOne
//    @JoinColumn(name = "state_id")
//    private EmailState emailState;
    @Column(name = "update_date")
    private Timestamp updateDate;

    public Email() {

    }
    public Email(Long emailId, String emailFrom, String emailTo, String emailCC, String emailBody, int state, Timestamp updateDate) {
        this.emailId = emailId;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.emailCC = emailCC;
        this.emailBody = emailBody;
        this.state = state;
        this.updateDate = getCurrentDate();
    }

    public Email(EmailDTO emailDTO) {
        this.emailId = emailDTO.getEmailId();
        this.emailFrom = (emailDTO.getEmailFrom() != null) ? emailDTO.getEmailFrom() : "default_user@gbtec.es";
        this.emailTo = getEmailsString(emailDTO.getEmailTo());
        this.emailCC = getEmailsString(emailDTO.getEmailCC());
        this.emailBody = (emailDTO.getEmailBody() != null) ? emailDTO.getEmailBody() : "";
        this.state = emailDTO.getState();
        this.updateDate = getCurrentDate();
    }

    public static Timestamp getCurrentDate() {
        // Obtiene la fecha y hora actual en milisegundos
        long tiempoMillis = System.currentTimeMillis();

        // Crea un Timestamp a partir del tiempo actual en milisegundos
        Timestamp fechaHoraActual = new Timestamp(tiempoMillis);

        return fechaHoraActual;
    }

    private static String getEmailsString(List<RecipientDTO> emailList) {
        String emailString;
        if(emailList != null) {
            emailString = emailList.stream()
                    .map(RecipientDTO::getEmail)
                    .collect(Collectors.joining(";"));
        } else {
            emailString = "";
        }
        return emailString;
    }

    // Getters y setters

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long id) {
        this.emailId = id;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getEmailCC() {
        return emailCC;
    }

    public void setEmailCC(String emailCC) {
        this.emailCC = emailCC;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        if(updateDate != null) {
            this.updateDate = updateDate;
        } else {
            this.updateDate = getCurrentDate();
        }
    }
}