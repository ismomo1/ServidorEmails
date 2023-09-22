package net.javaguides.springboot.model;

import java.util.List;

public class EmailListDTO {
    private List<EmailDTO> emails;

    public EmailListDTO() {

    }

    public EmailListDTO (List<EmailDTO> emailDTOs) {
        this.emails = emailDTOs;
    }

    public List<EmailDTO> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailDTO> emails) {
        this.emails = emails;
    }
}
