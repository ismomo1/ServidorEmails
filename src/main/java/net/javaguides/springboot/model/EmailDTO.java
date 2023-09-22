package net.javaguides.springboot.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmailDTO {
    private Long emailId;
    private String emailFrom;
    private List<RecipientDTO> emailTo;
    private List<RecipientDTO> emailCC;
    private String emailBody;
    private int state;
    private Timestamp updateDate;

    public EmailDTO(Long emailId, String emailFrom, List<RecipientDTO> emailTo, List<RecipientDTO> emailCC, String emailBody, int state, Timestamp updateDate) {
        this.emailId = emailId;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.emailCC = emailCC;
        this.emailBody = emailBody;
        this.state = state;
        this.updateDate = Email.getCurrentDate();
    }

    public EmailDTO(Email email) {
        this.emailId = email.getEmailId();
        this.emailFrom = email.getEmailFrom();
        this.emailTo = getEmailList(email.getEmailTo());
        this.emailCC = getEmailList(email.getEmailCC());
        this.emailBody = email.getEmailBody();
        this.state = email.getState();
        this.updateDate = email.getUpdateDate();
    }

    public List<RecipientDTO> getEmailList(String emailString) {
        if (emailString != null) {
            List<String> emailList = Arrays.asList(emailString.split(";"));
            return emailList.stream()
                    .map(recipient -> new RecipientDTO(recipient))
                    .collect(Collectors.toList());
        }
        return null;
    }

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

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }
}