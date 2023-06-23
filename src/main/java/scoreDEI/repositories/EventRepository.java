package scoreDEI.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import scoreDEI.data.Event;

public interface EventRepository extends CrudRepository<Event, Integer> {
    public List<Event> findAllByOrderByIdAsc();

    public List<Event> findByGameIdOrderByMinuteAsc(int gameId);

}
