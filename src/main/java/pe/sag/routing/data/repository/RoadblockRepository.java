package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.Roadblock;

@Repository
public interface RoadblockRepository extends MongoRepository<Roadblock, String> {
}
