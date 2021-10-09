package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Truck;

public interface TruckRepository extends MongoRepository<Truck,String> {
}
