package scoreDEI.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scoreDEI.repositories.GameRepository;
import scoreDEI.data.Game;

@Service
public class GameService {
    @Autowired 
    GameRepository gameRepo;

    public List<Game> getAllGames(){
        List<Game> games = new ArrayList<>(); 
        gameRepo.findAll().forEach(games::add);  

        return games;
    }

    public void saveGame(Game game){
        gameRepo.save(game);
    }

    public Game getGame(int id){
        Optional<Game> opt = gameRepo.findById(id);
        if(opt.isPresent())
            return opt.get();
        else
            return null;
    }

    public List<Game> getGamesByFinished(boolean finished){
        List<Game> games = gameRepo.findGameByFinished(finished);

        return games;
    }

    public List<Game> getGamesByRunningAndFinished(boolean running, boolean finished){
        List<Game> games = gameRepo.findGameByRunningAndFinished(running, finished);

        return games;
    }

    public void deleteGame(int id){
        gameRepo.deleteById(id);
    }
}
