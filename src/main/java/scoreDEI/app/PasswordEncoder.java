package scoreDEI.app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {
    
    
    public PasswordEncoder(){
    }

    public String encode(String str){ 
        MessageDigest digest = null;
        try{
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch(Exception e){
            System.out.println("PasswordEncoder - " + e.getMessage());
        }
        byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        String hashedStr = new String(hash);
        return hashedStr;   
    }

}
