package pe.sag.routing.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.sag.routing.core.model.Route;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteRepository extends MongoRepository<Route,String> {
    List<Route> findAllByStartDateIsAfterAndFinishDateIsBeforeAndMonitoring(LocalDateTime startDate, LocalDateTime finishDate, boolean monitoring);
}
