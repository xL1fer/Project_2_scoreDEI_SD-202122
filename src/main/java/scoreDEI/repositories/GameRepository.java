package scoreDEI.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import scoreDEI.data.Game;

public interface GameRepository extends CrudRepository<Game, Integer> {
    public List<Game> findGameByFinished(boolean finished);

    public List<Game> findGameByRunningAndFinished(boolean running, boolean finished);
}
