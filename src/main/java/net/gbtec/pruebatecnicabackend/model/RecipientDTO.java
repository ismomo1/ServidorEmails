package net.gbtec.pruebatecnicabackend.model;

public class RecipientDTO {
    private String email;

    public RecipientDTO() {
    }

    public RecipientDTO(String email) {
        this.email = email;
    }

    // Getters y setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}