package scoreDEI.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scoreDEI.data.Team;
import scoreDEI.repositories.TeamRepository;

@Service
public class TeamService {
    @Autowired
    TeamRepository teamRepo;

    public void saveTeam(Team team){
        teamRepo.save(team);
    }

    public List<Team> getAllTeams(){
        List<Team> teams = new ArrayList<>();
        teamRepo.findAllByOrderByIdAsc().forEach(teams::add);
        return teams;
    }

    public List<Team> getAllTeamsByWonGames() {
        return teamRepo.findAllByOrderByWonGamesDesc();
    }

    public List<Team> getAllTeamsByLostGames() {
        return teamRepo.findAllByOrderByLostGamesDesc();
    }

    public List<Team> getAllTeamsByTiedGames() {
        return teamRepo.findAllByOrderByTiedGamesDesc();
    }

    public Team getTeam(int id){
        Optional<Team> opt = teamRepo.findById(id);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    public Team getTeamByApiID(int apiID){
        Optional<Team> opt = teamRepo.findByApiID(apiID);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    public void deleteTeam(int id) {
        teamRepo.deleteById(id);
    }
}
