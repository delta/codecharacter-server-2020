package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeVersion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CodeVersionRepository extends MongoRepository<CodeVersion, Integer> {
    List<CodeVersion> getAllByUserId(Integer userId);
}
