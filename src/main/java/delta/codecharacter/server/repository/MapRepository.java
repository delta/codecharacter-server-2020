package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Map;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends MongoRepository<Map, Integer> {

}
