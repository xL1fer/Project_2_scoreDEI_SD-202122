package scoreDEI.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

/*
    Event Type and Event Description correlation:

    < 1 > (Goal)
        Goal Type:

        1 -> Normal
        2 -> Penalty
        3 -> Free kick

    < 2 > (Interruption)
        Interruption Type:

        1 -> Start
        2 -> Stopped
        3 -> Resumed
        4 -> Finish

    < 3 > (Foul)
        Foul Type:

        1 -> Yellow
        2 -> Red

    < 4 > (Substitution)
        Substitution Type:
*/

@Entity
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @NotNull(message = "Minute cannot be null.")
    Integer minute;
    Integer description;

    @Max(value = 4, message = "Event type should be less than 5.")
    @Min(value = 1, message = "Event type should be greater than 0.")
    int eventType; //1 - Goal, 2 - Interruption, 3 - Foul, 4 - Substitution

    @ManyToOne()
    Game game;

    // Goal/Foul Player
    @ManyToOne()
    Player scorer_foulMaker;

    // Substitution Players
    @ManyToOne()
    Player playerIn;

    @ManyToOne()
    Player playerOut;

    public Event() {
    }

    // Goal/Fould constructor
    public Event(Integer minute, Integer description, int eventType, Player scorer_foulMaker) {
        this.minute = minute;
        this.description = description;
        this.eventType = eventType;
        this.scorer_foulMaker = scorer_foulMaker;
    }

    // Interruption constructor
    public Event(Integer minute, Integer description, int eventType) {
        this.minute = minute;
        this.description = description;
        this.eventType = eventType;
    }

    // Substitution constructor
    public Event(Integer minute, Integer description, int eventType, Player playerIn, Player playerOut) {
        this.minute = minute;
        this.description = description;
        this.eventType = eventType;
        this.playerIn = playerIn;
        this.playerOut = playerOut;
    }

    public Event(Integer minute, Integer description) {
        this.minute = minute;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMinute() {
        return minute;
    }
    
    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Integer getDescription() {
        return description;
    }

    public void setDescription(Integer description) {
        this.description = description;
    }

    public Player getScorer_foulMaker() {
        return scorer_foulMaker;
    }

    public void setScorer_foulMaker(Player scorer_foulMaker) {
        this.scorer_foulMaker = scorer_foulMaker;
    }

    public Player getPlayerIn() {
        return playerIn;
    }

    public void setPlayerIn(Player playerIn) {
        this.playerIn = playerIn;
    }

    public Player getPlayerOut() {
        return playerOut;
    }

    public void setPlayerOut(Player playerOut) {
        this.playerOut = playerOut;
    }

    public String descriptionParser() {
        if (this.eventType == 1) {
            if (this.description == 1) return "Normal";
            else if (this.description == 2) return "Penalty";
            else if (this.description == 3) return "Free kick";
        }

        else if (this.eventType == 2) {
            if (this.description == 1) return "Start";
            else if (this.description == 2) return "Stopped";
            else if (this.description == 3) return "Resumed";
            else if(this.description == 4) return "Finish";
        }

        else if (this.eventType == 3) {
            if (this.description == 1) return "Yellow";
            else if (this.description == 2) return "Red";
        }
        return "Unknown";
    }
}
