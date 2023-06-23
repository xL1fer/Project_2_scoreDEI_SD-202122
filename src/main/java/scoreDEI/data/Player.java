package scoreDEI.data;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.persistence.GenerationType;

@Entity
@Validated
public class Player {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank(message = "Player first name cannot be blank.")
    private String firstname;
    @NotBlank(message = "Player last name cannot be blank.")
    private String lastname;

    @NotNull(message = "Player needs to have a team.")
    @ManyToOne()
    private Team team;

    private Integer age;
    
    @NotNull(message = "Player birthdate cannot be null.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    @NotBlank(message = "Player nationality cannot be blank.")
    private String nationality;
    private int goals;
    
    @NotBlank(message = "Player position cannot be blank.")
    private String position;

    public Player(){
    }

    // this constructor is used to create a player with name, birthdate, team and position (as specified in project statement 4.1.3)
    public Player(String firstname, LocalDate birthdate, Team team, String position) {
        this.firstname = firstname;
        this.lastname = "";
        this.age = -1;
        this.birthdate = birthdate;
        this.nationality = "";
        this.goals = 0;
        this.team = team;
        this.position = position;
    }

    public Player(String firstname, String lastname, Integer age, LocalDate birthdate, String nationality, int goals, Team team, String position) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
        this.birthdate = birthdate;
        this.nationality = nationality;
        this.goals = goals;
        this.team = team;
        this.position = position;
    }

    public void addGoal(){
        this.goals++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public int getGoals() {
        return goals;
    }

    public void setGoals(int goals) {
        this.goals = goals;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String toString(){
        String str = firstname + " " + lastname + " (" + team.getName() + ")";
        return str;
    }

}
