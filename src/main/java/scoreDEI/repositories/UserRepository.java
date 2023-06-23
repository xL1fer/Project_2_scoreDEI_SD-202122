package scoreDEI.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import scoreDEI.data.UserClient;

public interface UserRepository extends CrudRepository<UserClient, Integer> {
    public List<UserClient> findAllByOrderByUsernameAsc();

    public Optional<UserClient> findByUsername(String username);

    public Optional<UserClient> findByAuthToken(String authToken);

    public void deleteUserClientByUsername(String username);
}
