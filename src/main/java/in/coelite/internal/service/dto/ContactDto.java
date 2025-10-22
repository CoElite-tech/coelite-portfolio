package in.coelite.internal.portfolio.dto;

public class ContactDto {
    String name;
    String email;
    String message;

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getMessage() {
        return message;
    }
    @Override
    public String toString() {
        return "ContactDto [name=" + name + ", email=" + email + ", message=" + message + "]";
    }

}
