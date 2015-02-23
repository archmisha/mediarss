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
    private boolean isAdmin;

    public static UserData createUser(String username, String password) {
        UserData user = new UserData();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public static UserData createAdminUser(String username, String password) {
        UserData user = UserData.createUser(username, password);
        user.setAdmin(true);
        return user;
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
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
