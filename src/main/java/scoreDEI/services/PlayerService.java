package scoreDEI.services;

import scoreDEI.data.Player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import scoreDEI.repositories.PlayerRepository;

@Service
public class PlayerService {
    @Autowired
    PlayerRepository playerRepo;

    public List<Player> getAllPlayers(){
        List<Player> players = new ArrayList<>(); 
        playerRepo.findAllByOrderByIdAsc().forEach(players::add);  
        return players;
    }

    public void savePlayer(Player player){
        playerRepo.save(player);
    }

    public Player getPlayer(int id){
        Optional<Player> opt = playerRepo.findById(id);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    public void deletePlayer(int id){
        playerRepo.deleteById(id);
    }

    public List<Player> getTeamPlayers(int id) {
        return playerRepo.findPlayerByTeamId(id);
    }

    public Player getBestScorer(){
        Optional<Player> opt = playerRepo.findTopByOrderByGoalsDesc();
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }
}
