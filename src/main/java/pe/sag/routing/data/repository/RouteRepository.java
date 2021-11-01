package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends MongoRepository<Route,String> {
    Optional<Route> findTopByTruckIdAndMonitoringOrderByFinishDateDesc(String truck, boolean monitoring);
    Optional<Route> findByTruckIdAndMonitoringAndStartDateBeforeAndFinishDateAfter(String truckId, boolean monitoring, LocalDateTime startDate, LocalDateTime finishDate);
    List<Route> findByStartDateBeforeAndFinishDateAfterAndMonitoring(LocalDateTime startDate, LocalDateTime finishDate, boolean monitoring);
    List<Route> findByMonitoring(boolean monitoring);
    void deleteByMonitoring(boolean monitoring);
}
