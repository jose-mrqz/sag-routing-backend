package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.TruckModel;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends MongoRepository<Truck,String> {
    Optional<Truck> findTopByModelOrderByCodeDesc(TruckModel model);
    List<Truck> findByMonitoring(boolean monitoring);
    List<Truck> findByCode(String code);
    Optional<Truck> findByCodeAndMonitoring(String code, boolean monitoring);
    List<Truck> findByMonitoringAndStatusOrderByModelDesc(boolean monitoring, String status);
    int deleteByCode(String code);
}
