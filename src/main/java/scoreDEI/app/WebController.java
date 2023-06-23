package scoreDEI.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import scoreDEI.data.Event;
import scoreDEI.data.Game;
import scoreDEI.data.Player;
import scoreDEI.data.Team;
import scoreDEI.data.UserClient;
import scoreDEI.services.EventService;
import scoreDEI.services.GameService;
import scoreDEI.services.PlayerService;
import scoreDEI.services.TeamService;
import scoreDEI.services.UserService;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/*
*   Api-Sports Keys
*
*   6ac3db982e5b3a7797a4dc7295e5a183 -> chave Leopoldo
*   002376ccf1e18bf6c617c9dab49e1f5e -> chave Luís
*
*/

@Controller
public class WebController {

    static String apiKey = "6ac3db982e5b3a7797a4dc7295e5a183";

    @Autowired
    PlayerService playerService;

    @Autowired
    TeamService teamService;

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    GameService gameService;

    @GetMapping("/")
    public String index(Model model, HttpSession session) {

        checkTokenExpiration(session);

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if(checkTokenExpiration(session) != 0){
            List<String> modelErrors = new ArrayList<>();                                  // create errors array
            modelErrors.add("You are already logged in.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        UserClient userLogin = new UserClient();

        model.addAttribute("userLogin", userLogin);

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute(value="loginForm") UserClient userLogin, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();  
        if(checkTokenExpiration(session) != 0){
            //user is already logged                                // create errors array
            modelErrors.add("You are already logged in.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        String authToken = userService.authenticateUser(userLogin);
        if(authToken != null){
            //assign token and username to session
            session.setAttribute("authToken", authToken);
            session.setAttribute("username", userLogin.getUsername());
            return "redirect:/";
        }
        else{
            session.removeAttribute("authToken");

            modelErrors.add("Invalid login information."); //add message
            model.addAttribute("errors", modelErrors);
            
            model.addAttribute("userLogin", userLogin);

            return "login";
        }
    }

    @GetMapping("/register")
    public String register(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if(checkTokenExpiration(session) != 0){
            //user is already logged
            List<String> modelErrors = new ArrayList<>();                                  // create errors array
            modelErrors.add("You are already logged in.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        UserClient userRegister = new UserClient();
        model.addAttribute("userRegister", userRegister);

        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute UserClient userRegister, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>(); //create the errors array

        if(checkTokenExpiration(session) != 0){
            //user is already logged
            modelErrors.add("You are already logged in.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        //validate user input
        if(validationResult.hasErrors()){
            //there's errors
            redirectAttributes.addFlashAttribute("userRegister", userRegister); //send the user info again

            List<ObjectError> objectErrors = validationResult.getAllErrors();
            
            for(ObjectError error : objectErrors){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            redirectAttributes.addFlashAttribute("errors", modelErrors); //add list to attribute
            
            return "redirect:/register"; //show user the register page again with the valid information he has already put
        }
        //input is valid
        System.out.println("input valid!");
        //register not successful
        if(!userService.registerUser(userRegister)){
            model.addAttribute("userRegister", userRegister);
            
            // this message might not be the case why register couldn't be done, but will stay as is for now
            modelErrors.add("Username already exists.");

            model.addAttribute("errors", modelErrors);

            //System.out.println("Register user failed!!");

            return "register";
        }

        //register WAS successful

        //automatically login user
        String authToken = userService.authenticateUser(userRegister);
        
        if(authToken != null){
            //assign token and username to session
            session.setAttribute("authToken", authToken);
            session.setAttribute("username", userRegister.getUsername());
            return "redirect:/";
        }
        else{
            //this should never happen
            //authentication right after register
            session.removeAttribute("authToken");            
            return "login";
        }
    }

    @GetMapping("/pageNotFound")
    public String pageNotFound(HttpSession session) {
        checkTokenExpiration(session);

        return "pageNotFound";
    }

    // This method is used to avoid user entering ".../logout" in page link and invoking a GET method exception

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {

        session.removeAttribute("authToken");
        List<String> modelErrors = new ArrayList<>();
        modelErrors.add("Successfully logged out.");
        redirectAttributes.addFlashAttribute("errors", modelErrors);

        return "redirect:/";
    }

    @GetMapping("/teamTable")
    public String teamTable(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();

        if(checkTokenExpiration(session) != 2){

            modelErrors.add("You need admin privileges to access that page.");             // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("teams", this.teamService.getAllTeams());
        return "teamTable";
    }

    @GetMapping("/createTeam")
    public String createTeam(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        if(checkTokenExpiration(session) != 2){

            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("team", new Team());
        return "manageTeam";
    }

    @GetMapping("/editTeam")
    public String editTeam(@RequestParam(name="id", required=false) Integer id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();
        if(checkTokenExpiration(session) != 2){

            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        // in case user tries to edit a team through link and does not specify an id
        if (id == null) {
            modelErrors.add("Invalid team id.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/teamTable";
        }

        Team teamPresent = this.teamService.getTeam(id);
        if (teamPresent != null) {
            model.addAttribute("team", teamPresent);
            return "manageTeam";
        }
        // team id not found
        else {
            modelErrors.add("Team id not found.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/teamTable";
        }
    }

    @GetMapping("/saveTeam")
    public String saveTeam() {
        return "redirect:/";
    }

    @PostMapping("/saveTeam")
    public String saveTeam(@Valid @ModelAttribute Team team, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>(); //create the errors array
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        //validate user input
        if(validationResult.hasErrors()){

            //userRegister.setUsername(null);
            //there's errors
            team.setName(null);
            model.addAttribute("team", team);
            //System.out.println("Register Form not valid!!!");
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors);
            
            return "manageTeam"; //show user the register page again with the valid information he has already put
        }
        //input is valid
        
        teamService.saveTeam(team);

        return "redirect:/teamTable";
    }

    @GetMapping("/deleteTeam")
    public String deleteTeam() {
        return "redirect:/";
    }

    @PostMapping("/deleteTeam")
    public String deleteTeam(@ModelAttribute Team team, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>(); //create the errors array

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        teamService.deleteTeam(team.getId());
        return "redirect:/teamTable";
    }

    @GetMapping("/playerTable")
    public String playerTable(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>(); //create the errors array

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("players", this.playerService.getAllPlayers());
        return "playerTable";
    }

    @GetMapping("/createPlayer")
    public String createPlayer(Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("player", new Player());

        List<Team> teams = teamService.getAllTeams();

        // verify if at least one team exists
        if (teams.size() < 1) {                                  // create errors array
            modelErrors.add("You must create a team first.");                           // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/playerTable";
        }

        model.addAttribute("teams", teams);

        return "managePlayer";
    }

    @GetMapping("/editPlayer")
    public String editPlayer(@RequestParam(name="id", required=false) Integer id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        // in case user tries to edit a player through link and does not specify an id
        if (id == null) {
            modelErrors.add("Invalid player id.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/playerTable";
        }

        Player playerPresent = this.playerService.getPlayer(id);
        if (playerPresent != null) {
            model.addAttribute("player", playerPresent);
            
            List<Team> teams = teamService.getAllTeams();

            // verify if at least one team exists (this should never happen, just a secure statement)
            if (teams.size() < 1) {
                modelErrors.add("You must create a team first.");                                        // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/playerTable";
            }

            model.addAttribute("teams", teams);
            return "managePlayer";
        }
        // player id was not found
        else {
            modelErrors.add("Player id not found.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/playerTable";
        }
    }

    @GetMapping("/savePlayer")
    public String savePlayer() {
        return "redirect:/";
    }

    @PostMapping("/savePlayer")
    public String savePlayer(@Valid @ModelAttribute Player player, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        //validate user input
        if(validationResult.hasErrors()){

            //userRegister.setUsername(null);
            //there's errors
            player.setFirstname(null);
            model.addAttribute("player", player); //send the user info again
            model.addAttribute("teams", teamService.getAllTeams());
            //System.out.println("Register Form not valid!!!");
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors); //add list to attribute
            
            return "managePlayer"; //show user the register page again with the valid information he has already put
        }
        //input is valid

        //get team
        Team team = teamService.getTeam(player.getTeam().getId());
        //set team to player
        player.setTeam(team);
        //save player
        playerService.savePlayer(player);
        return "redirect:/playerTable";
    }

    @GetMapping("/deletePlayer")
    public String deletePlayer() {
        return "redirect:/";
    }

    @PostMapping("/deletePlayer")
    public String deletePlayer(@ModelAttribute Player player, HttpSession session, RedirectAttributes redirectAttributes){
        List<String> modelErrors = new ArrayList<>();

        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        playerService.deletePlayer(player.getId());
        return "redirect:/players";
    }

    @GetMapping("/userTable")
    public String userTable(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();

        //TODO: Remove this as comment
        /*
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }
        */

        model.addAttribute("users", this.userService.getAllUsers());
        return "userTable";
    }

    @GetMapping("/createUser")
    public String createUser(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        //TODO: Remove this as comment
        /*
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }
        */
        
        model.addAttribute("userRegister", new UserClient());
        return "manageUser";
    }

    @GetMapping("/editUser")
    public String editUser(@RequestParam(name="username", required=false) String username, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();
        //TODO: Remove this as comment
        /*
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }
        */

        // in case user tries to edit a user through link and does not specify an id
        if (username == null) {
            modelErrors.add("Invalid username.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/userTable";
        }

        UserClient userPresent = this.userService.getUser(username);
        if (userPresent != null) {
            model.addAttribute("userRegister", userPresent);
            return "manageUser";
        }
        // user id not found
        else {
            modelErrors.add("Username not found.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/userTable";
        }
    }

    @GetMapping("/saveUser")
    public String saveUser() {
        return "redirect:/";
    }

    // save user when creating one
    @PostMapping("/saveUser")
    public String saveUser(@Valid @ModelAttribute UserClient userRegister, BindingResult validationResult, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();
        //TODO: Remove this as comment
        /*
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }
        */


        //validate user input
        if(validationResult.hasErrors()){

            userRegister.setUsername(null);
            //there's errors
            model.addAttribute("userRegister", userRegister); //send the user info again
            //System.out.println("Register Form not valid!!!");
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors); //add list to attribute
            
            return "manageUser"; //show user the register page again with the valid information he has already put
        }
        //input is valid
        
        if(userService.registerUser(userRegister) == false){
            modelErrors.add("Cannot register already registered username.");
            redirectAttributes.addFlashAttribute("errors", modelErrors);
        }

        return "redirect:/userTable";
    }

    @GetMapping("/editSaveUser")
    public String editSaveUser() {
        return "redirect:/";
    }

    // save user when editing one
    @PostMapping("/editSaveUser")
    public String editSaveUser(@Valid @ModelAttribute UserClient userRegister, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        //TODO: Remove this as comment
        /*
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }
        */

        //validate user input
        if(validationResult.hasErrors()){

            //userRegister.setUsername(null);
            //there's errors
            model.addAttribute("userRegister", userRegister); //send the user info again
            //System.out.println("Register Form not valid!!!");

            List<ObjectError> objectErrors = validationResult.getAllErrors();
            
            for(ObjectError error : objectErrors){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors); //add list to attribute
            
            return "manageUser"; //show user the register page again with the valid information he has already put
        }
        //input is valid
        
        userService.adminRegisterUser(userRegister);

        return "redirect:/userTable";
    }

    @GetMapping("/deleteUser")
    public String deleteUser() {
        return "redirect:/";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@ModelAttribute UserClient user, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        userService.deleteUser(user.getUsername());
        return "redirect:/userTable";
    }

    @GetMapping("/eventTable")
    public String eventTable(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("events", this.eventService.getAllEvents());
        return "eventTable";
    }

    @GetMapping("/createEvent")
    public String createEvent(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("event", new Event());

        List<Game> games = gameService.getAllGames();

        // verify if at least one game exists
        if (games.size() < 1) {
            modelErrors.add("You must create a game first.");                           // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }

        model.addAttribute("games", games);

        model.addAttribute("players", playerService.getAllPlayers());

        return "manageEvent";
    }

    @GetMapping("/editEvent")
    public String editEvent(@RequestParam(name="id", required=false) Integer id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        // in case user tries to edit an event through link and does not specify an id
        if (id == null) {
            modelErrors.add("Invalid event id.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }

        Event eventPresent = this.eventService.getEvent(id);
        if (eventPresent != null) {
            model.addAttribute("event", eventPresent);

            List<Game> games = gameService.getAllGames();

            // verify if at least one game exists (this should never happen, just a secure statement)
            if (games.size() < 1) {
                modelErrors.add("You must create a game first.");                           // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/eventTable";
            }

            model.addAttribute("games", games);

            List<Player> homePlayers = playerService.getTeamPlayers(eventPresent.getGame().getHomeTeam().getId());
            List<Player> awayPlayers = playerService.getTeamPlayers(eventPresent.getGame().getAwayTeam().getId());
            
            for(Player p : awayPlayers){
                homePlayers.add(p);
            }

            //home players is now ALL players in both teams
            model.addAttribute("players", homePlayers);

            return "manageEvent";
        }
        // event id not found
        else {
            modelErrors.add("Event id not found.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }
    }

    @GetMapping("/saveEvent")
    public String saveEvent() {
        return "redirect:/";
    }

    @PostMapping("/saveEvent")
    public String saveEvent(@Valid @ModelAttribute Event event, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        Game game = gameService.getGame(event.getGame().getId());

        //validate user input
        if(validationResult.hasErrors()){

            //there's errors
            event.setMinute(null);
            model.addAttribute("event", event); //send the user info again
            model.addAttribute("players", playerService.getAllPlayers());
            model.addAttribute("games", gameService.getAllGames());
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors); //add list to attribute
            
            return "manageEvent"; //show user the register page again with the valid information he has already put
        }
        //validate that players belong to any of the Teams of the game
        else{

            boolean err = false;
            //goal
            if(event.getEventType() == 1 || event.getEventType() == 3){
                //make sure scorer and foulmaker belong to the game
                if(event.getScorer_foulMaker().getTeam() != game.getHomeTeam() && event.getScorer_foulMaker().getTeam() != game.getAwayTeam()){
                   //there's errors
                   err = true;
                    event.setMinute(null);
                    modelErrors.add("Player doesnt belong in any team of the game.");
                }
            }
            //substitution
            else if(event.getEventType() == 4){
                //make sure player in belongs to the game
                if(event.getPlayerIn().getTeam() != game.getHomeTeam() && event.getPlayerIn().getTeam() != game.getAwayTeam()){
                    //there's errors
                    err = true;
                    event.setMinute(null);
                    modelErrors.add("Player In doesnt belong in any team of the game.");
                }
                //make sure player out belongs to the game
                if(event.getPlayerOut().getTeam() != game.getHomeTeam() && event.getPlayerOut().getTeam() != game.getAwayTeam()){
                    //there's errors
                    err = true;
                    event.setMinute(null);
                    modelErrors.add("Player Out doesnt belong in any team of the game.");
                }

            }

            if(err){
                //if there are errors, show them.
                model.addAttribute("event", event); //send the user info again
                model.addAttribute("players", playerService.getAllPlayers());
                model.addAttribute("games", gameService.getAllGames());
                model.addAttribute("errors", modelErrors);
                return "manageEvent";
            }
        }

        // game has already finished
        if (game.isFinished()) {
            modelErrors.add("Game has already finished.");                              // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }

        // check if event type is game start
        if(event.getEventType() == 2 && event.getDescription() == 1){
            // check if game can be started
            if (!game.isRunning()) {    // no need to check for is finished here, because it is checked above
                game.setRunning(true);
                game.setStopped(false);
                game.setStartTime(LocalDateTime.now());
                gameService.saveGame(game);
            }
            // in case game has already started
            else {
                modelErrors.add("Game has already started.");                                      // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/eventTable";
            }
        }
        //check if event type is game resume
        else if(event.getEventType() == 2 && event.getDescription() == 3){
            //check if game can be resumed
            if(game.isRunning() && game.isStopped()){
                game.setStopped(false);
                gameService.saveGame(game);
            }
            //give error message because game is NOT stopped
            else{
                modelErrors.add("Game is not stopped and cannot be resumed.");                  // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/eventTable";
            }
        }
        // if game has not started yet and the event is not "game started", we cannot add events
        else if(!game.isRunning()){     // no need to check for is finished here, because it is checked above
            modelErrors.add("Game has not started yet.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }
        //if game is stopped we cannot add events
        else if(game.isStopped()){
            modelErrors.add("Game is stopped.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/eventTable";
        }

        //input is valid
        
        //score goal
        if(event.getEventType() == 1){
            Player scorer = event.getScorer_foulMaker();

            //if scorer belongs to home team
            if(scorer.getTeam().getId() == game.getHomeTeam().getId()){
                game.setHomeGoals(game.getHomeGoals() + 1);
            }
            //scorer belongs to away team
            else{
                game.setAwayGoals(game.getAwayGoals() + 1);
            }

            scorer.setGoals(scorer.getGoals() + 1);
            playerService.savePlayer(scorer);

        }
        

        //event is interruption
        if(event.getEventType() == 2){

            //game stop interruption
            if(event.getDescription() == 2){
                game.setStopped(true);
            }

            //game finish interruption
            else if(event.getDescription() == 4){
                System.out.println("Game finished.");
                game.setRunning(false);
                game.setFinished(true);

                //home team won
                if(game.getHomeGoals() > game.getAwayGoals()){
                    System.out.println("Home team won.");
                    game.getHomeTeam().setWonGames(game.getHomeTeam().getWonGames() + 1);
                    game.getAwayTeam().setLostGames(game.getAwayTeam().getLostGames() + 1);
                }
                //away team won
                else if(game.getHomeGoals() < game.getAwayGoals()){
                    System.out.println("Away team won.");
                    game.getAwayTeam().setWonGames(game.getAwayTeam().getWonGames() + 1);
                    game.getHomeTeam().setLostGames(game.getHomeTeam().getLostGames() + 1);
                }
                //teams tied
                else{
                    System.out.println("Teams tied.");
                    game.getHomeTeam().setTiedGames(game.getHomeTeam().getTiedGames() + 1);
                    game.getAwayTeam().setTiedGames(game.getAwayTeam().getTiedGames() + 1);
                }        
                gameService.saveGame(game);
            }
        }

        eventService.saveEvent(event);
        return "redirect:/eventTable";
    }

    @GetMapping("/deleteEvent")
    public String deleteEvent() {
        return "redirect:/";
    }

    @PostMapping("/deleteEvent")
    public String deleteEvent(@ModelAttribute Event event, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        eventService.deleteEvent(event.getId());
        return "redirect:/eventTable";
    }

    @GetMapping("/gameTable")
    public String gameTable(Model model, HttpSession session, RedirectAttributes redirectAttributes){
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("games", gameService.getAllGames());
        return "gameTable";
    }

    @GetMapping("/createGame")
    public String createGame(Model model, HttpSession session, RedirectAttributes redirectAttributes){
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        model.addAttribute("game", new Game());
        model.addAttribute("teams", teamService.getAllTeams());
        return "manageGame";
    }

    @GetMapping("/editGame")
    public String editGame(@RequestParam(name="id", required=false) Integer id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        // in case user tries to edit a user through link and does not specify an id
        if (id == null) {
            modelErrors.add("Invalid game id.");                                        // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/gameTable";
        }

        Game gamePresent = this.gameService.getGame(id);
        if (gamePresent != null) {
            model.addAttribute("game", gamePresent);

            List<Team> teams = teamService.getAllTeams();
            model.addAttribute("teams", teams);

            return "manageGame";
        }
        // user id not found
        else {
            modelErrors.add("Game id not found.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/gameTable";
        }
    }

    @GetMapping("/saveGame")
    public String saveGame(){
        return "redirect:/";
    }

    @PostMapping("/saveGame")
    public String saveGame(@Valid @ModelAttribute("game") Game game, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes){
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        //validate user input
        if(validationResult.hasErrors()){

            //userRegister.setUsername(null);
            //there's errors
            game.setLocation(null);
            model.addAttribute("game", game); //send the user info again
            model.addAttribute("teams", teamService.getAllTeams());
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            model.addAttribute("errors", modelErrors); //add list to attribute
            
            return "manageGame";
        }

        //verify if away team is not the same as home team
        if(game.getHomeTeam().getId() == game.getAwayTeam().getId()){
            modelErrors.add("Home team cannot be the same as Away team.");
            game.setLocation(null);
            model.addAttribute("game", game); //send the user info again
            model.addAttribute("teams", teamService.getAllTeams());
            model.addAttribute("errors", modelErrors);

            return "manageGame";
        }

        Team homeTeam = teamService.getTeam(game.getHomeTeam().getId());
        Team awayTeam = teamService.getTeam(game.getAwayTeam().getId());

        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);

        gameService.saveGame(game);
        return "redirect:/gameTable";
    }

    @GetMapping("/deleteGame")
    public String deleteGame() {
        return "redirect:/";
    }

    @PostMapping("/deleteGame")
    public String deleteGame(@ModelAttribute Game game, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need admin privileges to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/";
        }

        gameService.deleteGame(game.getId());
        return "redirect:/gameTable";
    }

    @GetMapping("/gameList")
    public String gameList(@RequestParam(name="page", defaultValue="1") Integer pageNum, @RequestParam(name="running", defaultValue="true") boolean running, @RequestParam(name="finished", defaultValue="false") boolean finished, Model model, HttpSession session){
        
        checkTokenExpiration(session);

        int gamesPerPage = 3;
        List<Game> gameList;

        //show running games
        if(running == true){
            finished = false;
            gameList = gameService.getGamesByRunningAndFinished(running, finished);
        }
        else{
            //running = false, so this can be finished games or games that havent started yet
            gameList = gameService.getGamesByRunningAndFinished(running, finished);
        }

        int maxPages = (int) Math.ceil((double) gameList.size() / gamesPerPage);
        if (maxPages == 0) maxPages++;

        if(pageNum > maxPages || pageNum <= 0){
            return "redirect:/gameList"; //redirect to page 1
        }

        List<Game> gamePage = new ArrayList<>();

        for(int i = gamesPerPage * (pageNum - 1); i < gamesPerPage * pageNum && i < gameList.size(); i++){
            gamePage.add(gameList.get(i));
        }

        model.addAttribute("finished", finished);
        model.addAttribute("running", running);
        model.addAttribute("games", gamePage);
        model.addAttribute("page", pageNum);
        model.addAttribute("maxPages", maxPages);

        return "gameList";
    }

    @GetMapping("/game")
    public String game(@RequestParam(name="id", defaultValue="-1") Integer gameId, Model model, HttpSession session, RedirectAttributes redirectAttributes){

        checkTokenExpiration(session);

        Game game = gameService.getGame(gameId);
        // game was not found
        if (game == null) {
            List<String> modelErrors = new ArrayList<>();

            modelErrors.add("Game id not found.");                         // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/gameList";
        }
        //game was found
        else {
            List<Event> eventList = eventService.getEventsByGameOrdered(game.getId());
            model.addAttribute("game", game);
            model.addAttribute("eventList", eventList);
        }

        return "game";
    }

    @GetMapping("/events")
    public String events() {
        return "events";
    }

    @GetMapping("/statistics")
    public String statistics(@RequestParam(name="filter", defaultValue="none") String filter, Model model, HttpSession session){

        checkTokenExpiration(session);

        // teams ordered by wins
        if (filter.compareTo("wins") == 0) {
            List<Team> teams = teamService.getAllTeamsByWonGames();
            model.addAttribute("teams", teams);
        }
        // teams ordered by losses
        else if (filter.compareTo("defeats") == 0) {
            List<Team> teams = teamService.getAllTeamsByLostGames();
            model.addAttribute("teams", teams);
        }
        // teams ordered by draws
        else if (filter.compareTo("draws") == 0) {
            List<Team> teams = teamService.getAllTeamsByTiedGames();
            model.addAttribute("teams", teams);
        }
        // default case
        else {
            //default case gets teams sorted by wins
            List<Team> teams = teamService.getAllTeamsByWonGames();
            filter = "wins";
            model.addAttribute("teams", teams);
        }

        model.addAttribute("bestScorer", playerService.getBestScorer());
        model.addAttribute("filter", filter);

        return "statistics";
    }

    @PostMapping("/addEvent")
    public String addEvent(@ModelAttribute Game gameParam, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes){
        List<String> modelErrors = new ArrayList<>();
        Game game = gameService.getGame(gameParam.getId());

        //if user is not logged
        if(checkTokenExpiration(session) == 0){
            modelErrors.add("You need to be logged to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/game?id=" + game.getId();
        }

        long minute;
        //if game is not running
        if(!game.isRunning()){
            minute = 0;
        }
        else{
            //if game is running, calculate current minute
            minute = game.getStartTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
        }

        Event event = new Event();
        event.setGame(game);
        event.setMinute((int) minute);

        List<Player> homePlayers = playerService.getTeamPlayers(game.getHomeTeam().getId());
        List<Player> awayPlayers = playerService.getTeamPlayers(game.getAwayTeam().getId());

        for(Player p : awayPlayers){
            homePlayers.add(p);
        }

        model.addAttribute("game", game);
        model.addAttribute("event", event);
        model.addAttribute("minute", minute);
        model.addAttribute("players", homePlayers);

        return "addEvent";
    }

    @GetMapping("/saveUserEvent")
    public String saveUserEvent() {
        return "redirect:/";
    }

    @PostMapping("/saveUserEvent")
    public String saveUserEvent(@Valid @ModelAttribute Event event, BindingResult validationResult, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        List<String> modelErrors = new ArrayList<>();
        
        if(checkTokenExpiration(session) != 2){
            //user is not an admin
            modelErrors.add("You need to be logged to access that page.");  // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/game?id=" + event.getGame().getId();
        }

        Game game = gameService.getGame(event.getGame().getId());

        //validate user input
        if(validationResult.hasErrors()){
            
            for(ObjectError error : validationResult.getAllErrors()){
                modelErrors.add(error.getDefaultMessage()); //add errors to list
            }

            redirectAttributes.addFlashAttribute("errors", modelErrors); //add list to attribute
            
            return "redirect:/game?id=" + event.getGame().getId();
        }
        //validate that players belong to any of the Teams of the game
        else{

            boolean err = false;
            //goal
            if(event.getEventType() == 1 || event.getEventType() == 3){
                //make sure scorer and foulmaker belong to the game
                if(event.getScorer_foulMaker().getTeam() != game.getHomeTeam() && event.getScorer_foulMaker().getTeam() != game.getAwayTeam()){
                   //there's errors
                   err = true;
                    event.setMinute(null);
                    modelErrors.add("Player doesnt belong in any team of the game.");
                }
            }
            //substitution
            else if(event.getEventType() == 4){
                //make sure player in belongs to the game
                if(event.getPlayerIn().getTeam() != game.getHomeTeam() && event.getPlayerIn().getTeam() != game.getAwayTeam()){
                    //there's errors
                    err = true;
                    event.setMinute(null);
                    modelErrors.add("Player In doesnt belong in any team of the game.");
                }
                //make sure player out belongs to the game
                if(event.getPlayerOut().getTeam() != game.getHomeTeam() && event.getPlayerOut().getTeam() != game.getAwayTeam()){
                    //there's errors
                    err = true;
                    event.setMinute(null);
                    modelErrors.add("Player Out doesnt belong in any team of the game.");
                }

            }

            if(err){
                //if there are errors, show them.
                model.addAttribute("errors", modelErrors);
                return "redirect:/game?id=" + event.getGame().getId();
            }
        }

        // game has already finished
        if (game.isFinished()) {
            modelErrors.add("Game has already finished.");                              // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/game?id=" + event.getGame().getId();
        }

        // check if event type is game start
        if(event.getEventType() == 2 && event.getDescription() == 1){
            // check if game can be started
            if (!game.isRunning()) {    // no need to check for is finished here, because it is checked above
                game.setRunning(true);
                game.setStopped(false);
                game.setStartTime(LocalDateTime.now());
                gameService.saveGame(game);
            }
            // in case game has already started
            else {
                modelErrors.add("Game has already started.");                                      // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/game?id=" + event.getGame().getId();
            }
        }
        //check if event type is game resume
        else if(event.getEventType() == 2 && event.getDescription() == 3){
            //check if game can be resumed
            if(game.isRunning() && game.isStopped()){
                game.setStopped(false);
                gameService.saveGame(game);
            }
            //give error message because game is NOT stopped
            else{
                modelErrors.add("Game is not stopped and cannot be resumed.");                  // add message
                redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request
    
                return "redirect:/game?id=" + event.getGame().getId();
            }
        }
        // if game has not started yet and the event is not "game started", we cannot add events
        else if(!game.isRunning()){     // no need to check for is finished here, because it is checked above
            modelErrors.add("Game has not started yet.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/game?id=" + event.getGame().getId();
        }
        //if game is stopped we cannot add events
        else if(game.isStopped()){
            modelErrors.add("Game is stopped.");                                      // add message
            redirectAttributes.addFlashAttribute("errors", modelErrors);    // add to redirect request

            return "redirect:/game?id=" + event.getGame().getId();
        }

        //input is valid
        
        //score goal
        if(event.getEventType() == 1){
            Player scorer = event.getScorer_foulMaker();

            //if scorer belongs to home team
            if(scorer.getTeam().getId() == game.getHomeTeam().getId()){
                game.setHomeGoals(game.getHomeGoals() + 1);
            }
            //scorer belongs to away team
            else{
                game.setAwayGoals(game.getAwayGoals() + 1);
            }

            scorer.setGoals(scorer.getGoals() + 1);
            playerService.savePlayer(scorer);

        }
        

        //event is interruption
        if(event.getEventType() == 2){

            //game stop interruption
            if(event.getDescription() == 2){
                game.setStopped(true);
            }

            //game finish interruption
            else if(event.getDescription() == 4){
                System.out.println("Game finished.");
                game.setRunning(false);
                game.setFinished(true);

                //home team won
                if(game.getHomeGoals() > game.getAwayGoals()){
                    System.out.println("Home team won.");
                    game.getHomeTeam().setWonGames(game.getHomeTeam().getWonGames() + 1);
                    game.getAwayTeam().setLostGames(game.getAwayTeam().getLostGames() + 1);
                }
                //away team won
                else if(game.getHomeGoals() < game.getAwayGoals()){
                    System.out.println("Away team won.");
                    game.getAwayTeam().setWonGames(game.getAwayTeam().getWonGames() + 1);
                    game.getHomeTeam().setLostGames(game.getHomeTeam().getLostGames() + 1);
                }
                //teams tied
                else{
                    System.out.println("Teams tied.");
                    game.getHomeTeam().setTiedGames(game.getHomeTeam().getTiedGames() + 1);
                    game.getAwayTeam().setTiedGames(game.getAwayTeam().getTiedGames() + 1);
                }        
                gameService.saveGame(game);
            }
        }

        eventService.saveEvent(event);
        return "redirect:/game?id=" + event.getGame().getId();
    }

    @GetMapping("/team")
    public String team(@RequestParam(name="id", defaultValue="-1") Integer teamId, Model model, HttpSession session){

        checkTokenExpiration(session);

        Team team = teamService.getTeam(teamId);
        // game was not found
        if (team == null) {
            List<String> modelErrors = new ArrayList<>();

            modelErrors.add("Team id not found.");                         // add message
            model.addAttribute("errors", modelErrors);    // add to redirect request

            return "team";
        }
        //game was found
        else {
            model.addAttribute("team", team);
        }

        return "team";
    }

    @GetMapping("/player")
    public String player(@RequestParam(name="id", defaultValue="-1") Integer playerId, Model model, HttpSession session){
        
        checkTokenExpiration(session);

        Player player = playerService.getPlayer(playerId);
        // player was not found
        if (player == null) {
            List<String> modelErrors = new ArrayList<>();

            modelErrors.add("Player id not found.");                         // add message
            model.addAttribute("errors", modelErrors);    // add to redirect request

            return "player";
        }
        //player was found
        else {
            model.addAttribute("player", player);
        }

        return "player";
    }

    @GetMapping("/addAPIData")
    public String addAPIData() {
        return "redirect:/";
    }

    @PostMapping("/addAPIData")
    public String addAPIData(RedirectAttributes redirectAttributes, HttpSession session) throws IOException {

        //only admins can do this
        if(checkTokenExpiration(session) != 2){
            redirectAttributes.addFlashAttribute("apiTeamsMessage", "Admin privileges are needed to add data from the API.");
            return "redirect:/";
        }

        URL url = new URL("https://v3.football.api-sports.io/teams?league=94&season=2021");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        //connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "scoreDEI");
        connection.setRequestProperty("x-apisports-key", apiKey);
        connection.setRequestProperty("x-apisports-host", "v3.football.api-sports.io");
        
        if(connection.getResponseCode() < 300){

            System.out.println("Remaining requests: " + connection.getHeaderField("x-ratelimit-requests-remaining"));

            InputStream is = connection.getInputStream();
            byte[] message = is.readAllBytes();
            String str = new String(message);
            JSONObject jsonObject = new JSONObject(str);

            JSONArray response = jsonObject.getJSONArray("response");
            
            for(int i = 0; i < response.length(); i++){
                JSONObject teamResponse = response.getJSONObject(i);
                JSONObject teamData = teamResponse.getJSONObject("team");
                String name = teamData.getString("name");
                String country = teamData.getString("country");
                String logo = teamData.getString("logo");
                int apiID = teamData.getInt("id");
                Team newTeam = new Team(name, country, logo, apiID, 0, 0, 0);
                teamService.saveTeam(newTeam);
            }
            
        }

        redirectAttributes.addFlashAttribute("apiTeamsMessage", "API Teams successfully created");

        int curPage = 1;
        int maxPage = -1;
        
        do{
            String urlStr = "https://v3.football.api-sports.io/players?league=94&season=2021&page=" + curPage;
            System.out.println("URLSTR: " + urlStr);
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            //connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "scoreDEI");
            connection.setRequestProperty("x-apisports-key", apiKey);
            connection.setRequestProperty("x-apisports-host", "v3.football.api-sports.io");
            
            if(connection.getResponseCode() < 300){

                System.out.println("Remaining requests: " + connection.getHeaderField("x-ratelimit-requests-remaining"));

                InputStream is = connection.getInputStream();
                byte[] message = is.readAllBytes();
                String str = new String(message);
                JSONObject jsonObject = new JSONObject(str);

                if(maxPage == -1){
                    maxPage = jsonObject.getJSONObject("paging").getInt("total");
                    System.out.println("Total pages: " + maxPage);
                }

                //curPage = jsonObject.getJSONObject("paging").getInt("current");

                JSONArray response = jsonObject.getJSONArray("response");
                
                for(int i = 0; i < response.length(); i++){
                    JSONObject playerResponse = response.getJSONObject(i);
                    JSONObject playerData = playerResponse.getJSONObject("player");

                    String firstname = playerData.getString("firstname");
                    String lastname = playerData.getString("lastname");

                    Integer age;
                    if(!playerData.isNull("age"))
                        age = playerData.getInt("age");
                    else
                        age = null;

                    String birthdateStr;
                    LocalDate birthdate;
                    if(!playerData.getJSONObject("birth").isNull("date")){
                        birthdateStr = playerData.getJSONObject("birth").getString("date");
                        birthdate = LocalDate.parse(birthdateStr);
                    }
                    else
                        birthdate = LocalDate.parse("0000-01-01");

                    String nationality = playerData.getString("nationality");
                    
                    int teamApiID = playerResponse.getJSONArray("statistics").getJSONObject(0).getJSONObject("team").getInt("id");

                    Team team = teamService.getTeamByApiID(teamApiID);

                    String position = playerResponse.getJSONArray("statistics").getJSONObject(0).getJSONObject("games").getString("position");

                    Player p = new Player(firstname, lastname, age, birthdate, nationality, 0, team, position);
                    playerService.savePlayer(p);
                }
            }

            System.out.println("CurPage: " + curPage);
            System.out.println("MaxPage: " + maxPage);
            curPage++;

            // temporary break to save some requests
            if (curPage == 20) break;
        }
        while(curPage <= maxPage);

        redirectAttributes.addFlashAttribute("apiPlayersMessage", "API Players successfully created");
        
        return "redirect:/";
    }

    /**
     * Checks the session for a token and validates it.
     * @param session Session to be validated
     * @return 0 if the token is invalid, 1 if the token belongs to a normal user, 2 if the token belongs to an admin
     */
    public int checkTokenExpiration(HttpSession session){
        String authToken = (String) session.getAttribute("authToken");

        if(authToken != null){
            int opt = userService.verifyUser(authToken);
            
            if(opt != 0)
                return opt;

        }

        session.removeAttribute("authToken");

        return 0;
    }
}
