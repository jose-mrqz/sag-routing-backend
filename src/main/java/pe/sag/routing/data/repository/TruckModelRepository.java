package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.TruckModel;

public interface TruckModelRepository extends MongoRepository<TruckModel,String> {
}
