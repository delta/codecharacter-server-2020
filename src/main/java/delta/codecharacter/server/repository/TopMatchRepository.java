package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.TopMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopMatchRepository extends MongoRepository<TopMatch, Integer> {

    Page<TopMatch> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
