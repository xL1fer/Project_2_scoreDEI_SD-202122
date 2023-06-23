package scoreDEI.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scoreDEI.app.PasswordEncoder;
import scoreDEI.data.UserClient;
import scoreDEI.repositories.UserRepository;

@Service
public class UserService{
    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    //returns true if successful, false if the user already exists
    public boolean registerUser(UserClient user){

        UserClient userPresent = findUser(user.getUsername());

        if(userPresent != null)
            return false;

        UserClient newUser = new UserClient(user.getUsername(), user.getEmail(), user.getPhone(), passwordEncoder.encode(user.getPassword()), user.isAdmin());
        saveUser(newUser);

        return true;
    }

    public void adminRegisterUser(UserClient user) {
        UserClient newUser = new UserClient(user.getUsername(), user.getEmail(), user.getPhone(), passwordEncoder.encode(user.getPassword()), user.isAdmin());
        saveUser(newUser);
    }

    //gets login information and creates a new token
    public String authenticateUser(UserClient user){

        //TODO: ERROR: invalid byte sequence for encoding "UTF8": 0x00 still happens from time to time, needs fix

        UserClient userPresent = findUser(user.getUsername());

        if(userPresent == null)  //user not found
            return null;
        else if(userPresent.getPassword().compareTo(passwordEncoder.encode(user.getPassword())) == 0){

            String authToken = userPresent.generateNewToken();

            saveUser(userPresent);

            return authToken;
        }

        return null;
    }

    public UserClient getUser(String username){
        Optional<UserClient> opt = userRepo.findByUsername(username);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    // this method needs to be "Transactional", otherwise a "No EntityManager with actual transaction" exception will occur
    @Transactional
    public void deleteUser(String username) {
        userRepo.deleteUserClientByUsername(username);
    }

    /**
     * Verifies a user authentication token.
     * @param authToken Token to be authenticated
     * @return 0 if the token is invalid, 1 if the token belongs to a normal user, 2 if the token belongs to an admin
     */
    public int verifyUser(String authToken){
        Optional<UserClient> userOpt = userRepo.findByAuthToken(authToken);

        if(userOpt.isPresent()){
            UserClient user = userOpt.get();

            //verify token expiration
            LocalDateTime time = LocalDateTime.now();
            //check if token is valid
            if(time.isAfter(user.getAuthTokenExpiration())){

                //token has expired
                return 0;
            }

            if(user.isAdmin())
                return 2;
            else
                return 1;
        }

        return 0;
    }

    public UserClient saveUser(UserClient user) {
        return userRepo.save(user);
    }

    public List<UserClient> getAllUsers(){
        List<UserClient> users = new ArrayList<>();
        userRepo.findAllByOrderByUsernameAsc().forEach(users::add);
        return users;
    }

    public UserClient findUser(String username) {
        Optional<UserClient> optUser = userRepo.findByUsername(username);

        if (optUser.isPresent())
            return optUser.get();
        
        return null;
    }
}
