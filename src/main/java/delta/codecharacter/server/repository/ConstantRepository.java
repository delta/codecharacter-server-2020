package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Constant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConstantRepository extends MongoRepository<Constant, Integer> {
    Constant findByKey(String key);
}
