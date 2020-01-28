package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Constant;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserStats;
import delta.codecharacter.server.util.Mode;
import delta.codecharacter.server.util.Verdict;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstantRepository extends MongoRepository<Constant, Integer> {
    Constant findByKey(String key);
}
