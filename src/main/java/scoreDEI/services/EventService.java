package scoreDEI.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scoreDEI.data.Event;
import scoreDEI.repositories.EventRepository;

@Service
public class EventService {
    @Autowired
    EventRepository eventRepo;

    public List<Event> getAllEvents(){
        List<Event> events = new ArrayList<>(); 
        eventRepo.findAllByOrderByIdAsc().forEach(events::add);  
        return events;
    }

    public Event getEvent(int id){
        Optional<Event> opt = eventRepo.findById(id);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    public List<Event> getEventsByGameOrdered(int gameId){
        return eventRepo.findByGameIdOrderByMinuteAsc(gameId);
    }

    public void saveEvent(Event event){
        eventRepo.save(event);
    }

    public void deleteEvent(int id) {
        eventRepo.deleteById(id);
    }
}
