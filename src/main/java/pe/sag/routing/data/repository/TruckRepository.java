package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.TruckModel;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends MongoRepository<Truck,String> {
    Optional<Truck> findTopByModelOrderByCodeDesc(TruckModel model);
    List<Truck> findByAvailableAndMonitoringOrderByModelDesc(boolean available, boolean monitoring);
    List<Truck> findByMonitoring(boolean monitoring);
}
