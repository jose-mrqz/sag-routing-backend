package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.SimulationInfo;

public interface SimulationInfoRepository extends MongoRepository<SimulationInfo,String> {
}
