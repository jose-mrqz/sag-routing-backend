package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.Depot;

@Repository
public interface DepotRepository extends MongoRepository<Depot, String> {
}
