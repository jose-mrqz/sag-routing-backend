package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pe.sag.routing.core.model.SimulationInfo;

@Repository
public interface SimulationInfoRepository extends MongoRepository<SimulationInfo,String> {
}
