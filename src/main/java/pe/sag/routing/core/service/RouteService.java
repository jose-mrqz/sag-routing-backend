package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.RouteRepository;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route register(Route route) {
        return routeRepository.save(route);
    }

    public Route edit(Route route) {
        return routeRepository.save(route);
    }

    public List<Route> list() {
        return routeRepository.findAll();
    }

    public List<Route> getActiveRoutes(LocalDateTime actualDate, boolean monitoring) {
        return routeRepository.findByStartDateBeforeAndFinishDateAfterAndMonitoring(actualDate, actualDate, monitoring);
    }

    public Route getLastRouteByTruckMonitoring(Truck truck, boolean monitoring) {
        Optional<Route> route = routeRepository.
                findTopByTruckIdAndMonitoringOrderByFinishDateDesc(truck.get_id(), monitoring);
        return route.orElse(null);
    }

    public void deleteByMonitoring(boolean monitoring){
        routeRepository.deleteByMonitoring(monitoring);
    }
}

