package scoreDEI.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

@Entity
@Validated
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Location cannot be empty.")
    private String location;

    @NotNull(message = "Date cannot be null.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    private boolean running;
    private boolean stopped;
    private boolean finished;

    @NotNull(message = "Home goals cannot be null.")
    private int homeGoals;

    @NotNull(message = "Away goals cannot be null.")
    private int awayGoals;

    @NotNull(message = "A game needs to have an Home Team.")
    @ManyToOne()
    private Team homeTeam;

    @NotNull(message = "A game needs to have an Away Team.")
    @ManyToOne()
    private Team awayTeam;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Event> eventList;

    public Game() {
    }

    //this contructor will be used to create new games
    public Game(String location, LocalDateTime date, Team homeTeam, Team awayTeam){
        this.location = location;
        this.date = date;
        this.running = false;
        this.stopped = true;
        this.finished = false;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    //this constructor will be used to create games based on the API
    public Game(String location, LocalDateTime date, boolean running, boolean stopped, boolean finished, int homeGoals, int awayGoals, Team homeTeam, Team awayTeam) {
        this.location = location;
        this.date = date;
        this.running = running;
        this.stopped = stopped;
        this.finished = finished;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.homeTeam  = homeTeam;
        this.awayTeam = awayTeam;
    }

    

    public Game(int id, String location, LocalDateTime date, boolean running, boolean stopped, boolean finished, int homeGoals,
            int awayGoals, Team homeTeam, Team awayTeam) {
        this.id = id;
        this.location = location;
        this.date = date;
        this.running = running;
        this.stopped = stopped;
        this.finished = finished;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(int homeGoals) {
        this.homeGoals = homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(int awayGoals) {
        this.awayGoals = awayGoals;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String dateFormated() {
        if (date == null) return "Undefined Date";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    public String toString(){
        String str = homeTeam.getName() + " vs " + awayTeam.getName()  + " (" + dateFormated() + ")";
        return str;
    }   
}
