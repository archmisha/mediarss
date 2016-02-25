package rss.test.entities;

/**
 * User: dikmanm
 * Date: 13/02/2015 11:54
 */
public class UserData {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean admin;
    private boolean validated;
    private long id;

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean isValidated) {
        this.validated = isValidated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
