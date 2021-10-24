package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Truck;

import java.util.List;

public interface TruckRepository extends MongoRepository<Truck,String> {
    List<Truck> findByAvailableAndMonitoringOrderByModelDesc(boolean available, boolean monitoring);
    List<Truck> findByMonitoring(boolean monitoring);
}
