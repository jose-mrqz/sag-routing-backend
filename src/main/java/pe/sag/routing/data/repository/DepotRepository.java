package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Depot;

public interface DepotRepository extends MongoRepository<Depot,String> {
}
