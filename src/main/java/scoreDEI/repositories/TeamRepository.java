package scoreDEI.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import scoreDEI.data.Team;

public interface TeamRepository extends CrudRepository<Team, Integer> {
    public List<Team> findAllByOrderByIdAsc();

    public Optional<Team> findByApiID(int apiID);

    public void deleteTeamById(int id);

    public List<Team> findAllByOrderByWonGamesDesc();

    public List<Team> findAllByOrderByLostGamesDesc();

    public List<Team> findAllByOrderByTiedGamesDesc();
}
