package scoreDEI.data;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
@Validated
public class Team {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Team name cannot be blank.")
    private String name;
    @NotBlank(message = "Team country cannot be blank.")
    private String country;
    private String logo;
    private int apiID;

    private int wonGames;
    private int lostGames;
    private int tiedGames;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> players;

    @OneToMany(mappedBy = "homeTeam", cascade = CascadeType.ALL)
    private List<Game> homeGames;
    
    @OneToMany(mappedBy = "awayTeam", cascade = CascadeType.ALL)
    private List<Game> awayGames;

    public Team(){
    }

    // this constructor is used to create a team with name and logo (as specified in project statement 4.1.2)
    public Team(String name, String logo) {
        this.name = name;
        this.country = "";
        this.logo = logo;
        apiID = -1;
    }

    public Team(String name, String country, String logo) {
        this.name = name;
        this.country = country;
        this.logo = logo;
        apiID = -1;
    }

    public Team(String name, String country, String logo, int apiID, int wonGames, int lostGames, int tiedGames) {
        this(name, country, logo);
        this.apiID = apiID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getApiID() {
        return apiID;
    }

    public void setApiID(int apiID) {
        this.apiID = apiID;
    }

    public int getWonGames() {
        return wonGames;
    }

    public void setWonGames(int wonGames) {
        this.wonGames = wonGames;
    }

    public int getLostGames() {
        return lostGames;
    }

    public void setLostGames(int lostGames) {
        this.lostGames = lostGames;
    }

    public int getTiedGames() {
        return tiedGames;
    }

    public void setTiedGames(int tiedGames) {
        this.tiedGames = tiedGames;
    }

    public List<Game> getHomeGames() {
        return homeGames;
    }

    public void setHomeGames(List<Game> homeGames) {
        this.homeGames = homeGames;
    }

    public List<Game> getAwayGames() {
        return awayGames;
    }

    public void setAwayGames(List<Game> awayGames) {
        this.awayGames = awayGames;
    }

    public void addHomeGame(Game game){
        this.homeGames.add(game);
    }

    public void addAwayGame(Game game){
        this.awayGames.add(game);
    }
}
