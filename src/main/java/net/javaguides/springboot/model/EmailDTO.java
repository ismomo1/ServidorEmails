package net.javaguides.springboot.model;

import java.util.List;

public class EmailDTO {
    private Long emailId;
    private String emailFrom;
    private List<RecipientDTO> emailTo;
    private List<RecipientDTO> emailCC;
    private String emailBody;
    private int state;

    // Getters y setters

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long emailId) {
        this.emailId = emailId;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public List<RecipientDTO> getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(List<RecipientDTO> emailTo) {
        this.emailTo = emailTo;
    }

    public List<RecipientDTO> getEmailCC() {
        return emailCC;
    }

    public void setEmailCC(List<RecipientDTO> emailCC) {
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
}