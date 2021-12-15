package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends MongoRepository<Route,String> {
    Optional<Route> findFirstByTruckIdAndMonitoringOrderByFinishDateDesc(String truck, boolean monitoring);
    Optional<Route> findByTruckIdAndMonitoringAndStartDateBeforeAndFinishDateAfter(String truckId, boolean monitoring, LocalDateTime startDate, LocalDateTime finishDate);
    List<Route> findByStartDateBeforeAndFinishDateAfterAndMonitoring(LocalDateTime startDate, LocalDateTime finishDate, boolean monitoring);
    List<Route> findByMonitoring(boolean monitoring);
    void deleteByMonitoring(boolean monitoring);
    List<Route> findByStartDateBeforeAndFinishDateAfterAndMonitoringAndCancelled(LocalDateTime actualDate, LocalDateTime actualDate1, boolean monitoring, boolean cancelled);
    List<Route> findByTruckIdAndStartDateAfterAndCancelled(String truckId, LocalDateTime now, boolean cancelled);

    List<Route> findByFinishDateBetweenAndMonitoringAndCancelled(LocalDateTime startDate, LocalDateTime finishDate, boolean monitoring, boolean cancelled);
}
