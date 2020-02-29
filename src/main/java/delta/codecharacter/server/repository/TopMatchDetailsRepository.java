package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.TopMatchDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopMatchDetailsRepository extends MongoRepository<TopMatchDetails, Integer> {

}
