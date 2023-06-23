package scoreDEI.repositories;

import scoreDEI.data.Player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface PlayerRepository extends CrudRepository<Player, Integer>{
    public List<Player> findAllByOrderByIdAsc();

    public List<Player> findPlayerByTeamId(int id);

    public Optional<Player> findTopByOrderByGoalsDesc();
}
