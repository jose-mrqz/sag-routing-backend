package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.TruckModel;

import java.util.Optional;

public interface TruckModelRepository extends MongoRepository<TruckModel,String> {
    Optional<TruckModel> findTopByCodeContaining(String code);
}
