package scoreDEI.data;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

@Entity
@Validated
public class UserClient {
    @Id @NotBlank(message = "Username cannot be blank.")
    private String username;

    @Email(message = "Invalid email address.") @NotBlank(message = "Email cannot be blank.")
    private String email;
    
    @NotBlank(message = "Phone number cannot be blank.")
    private String phone;

    @Size(min = 8, max = 100000, message = "Password must be at least 8 characters")
    private String password;

    private boolean isAdmin;

    private String authToken;
    
    private LocalDateTime authTokenExpiration;

    public UserClient() {
    }

    // constructor used when user registers by himself
    public UserClient(String username, String email, String phone, String password) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isAdmin = false;
        this.authToken = "default";
        this.authTokenExpiration = LocalDateTime.now();
    }

    // constructor used when an admin registers a user
    public UserClient(String username, String email, String phone, String password, boolean isAdmin) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.isAdmin = isAdmin;
        this.authToken = "default";
        this.authTokenExpiration = LocalDateTime.now();
    }

    public String generateNewToken(){
        SecureRandom random = new SecureRandom();
        byte randomBytes[] = new byte[24];
        random.nextBytes(randomBytes);
        String authToken = new String(randomBytes, StandardCharsets.UTF_8);
        setAuthToken(authToken);

        //LocalDateTime time = LocalDateTime.now().plusMinutes(30);
        LocalDateTime time = LocalDateTime.now().plusMinutes(30);

        setAuthTokenExpiration(time);

        // "\u0000" occurrences are being replaced by "" since PostgreSQL doesn't support storing NULL (\0x00) characters in text fields
        return authToken.replaceAll("\u0000", "");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public LocalDateTime getAuthTokenExpiration() {
        return authTokenExpiration;
    }

    public void setAuthTokenExpiration(LocalDateTime authTokenExpiration) {
        this.authTokenExpiration = authTokenExpiration;
    }

    @Override
    public String toString() {
        return "UserClient [authToken=" + authToken + ", authTokenExpiration=" + authTokenExpiration + ", email="
                + email + ", isAdmin=" + isAdmin + ", password=" + password + ", phone=" + phone + ", username="
                + username + "]";
    }
}
