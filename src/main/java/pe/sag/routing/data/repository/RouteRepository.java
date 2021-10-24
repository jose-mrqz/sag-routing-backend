package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends MongoRepository<Route,String> {
    List<Route> findAllByStartDateIsAfterAndFinishDateIsBefore(LocalDateTime startDate, LocalDateTime finishDate);
    Optional<Route> findTopByTruckIdAndMonitoringOrderByFinishDateDesc(String truck, boolean monitoring);
    List<Route> findAllByStartDateIsAfterAndFinishDateIsBeforeAndMonitoring(LocalDateTime startDate, LocalDateTime finishDate, boolean monitoring);
}
