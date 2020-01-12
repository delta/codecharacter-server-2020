package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CodeStatusRepository extends MongoRepository<CodeStatus, Integer> {
    CodeStatus findFirstByUserId(Integer userId);
}
