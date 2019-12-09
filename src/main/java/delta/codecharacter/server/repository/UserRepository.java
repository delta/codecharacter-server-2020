package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    User findFirstByOrderByIdDesc();
}
