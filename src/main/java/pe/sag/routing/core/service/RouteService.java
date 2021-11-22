package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.*;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.RouteRepository;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route save(Route route) {
        return routeRepository.save(route);
    }

    public Route edit(Route route) {
        return routeRepository.save(route);
    }

    public List<Route> list() {
        return routeRepository.findAll();
    }

    public List<Route> getActiveRoutes(LocalDateTime actualDate, boolean monitoring) {
        return routeRepository.findByStartDateBeforeAndFinishDateAfterAndMonitoringAndCancelled(actualDate, actualDate, monitoring, false);
    }

    public List<Route> findByMonitoring(boolean monitoring){
        return routeRepository.findByMonitoring(monitoring);
    }

    public Route getLastRouteByTruckMonitoring(Truck truck, boolean monitoring) {
        Optional<Route> route = routeRepository.
                findTopByTruckIdAndMonitoringOrderByFinishDateDesc(truck.get_id(), monitoring);
        return route.orElse(null);
    }

    public Route getCurrentByTruckId(String truckId, boolean monitoring) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Route> route = routeRepository.findByTruckIdAndMonitoringAndStartDateBeforeAndFinishDateAfter(truckId, monitoring, now, now);
        return route.orElse(null);
    }

    public void deleteByMonitoring(boolean monitoring){
        routeRepository.deleteByMonitoring(monitoring);
    }

    public LocalDateTime transformDate(SimulationInfo simulationInfo, LocalDateTime dateToConvert){
        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = NANOS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.plusNanos(differenceTransformReal);

        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }

    public Route transformRoute(Route route, SimulationInfo simulationInfo){
        //Route transformedRoute

        route.setStartDate(transformDate(simulationInfo,route.getStartDate()));
        route.setFinishDate(transformDate(simulationInfo,route.getFinishDate()));

        for(Route.Order o : route.getOrders()){
            o.setDeliveryDate(transformDate(simulationInfo,o.getDeliveryDate()));
        }

        return route;
    }

    public Route transformRouteReverse(Route route, SimulationInfo simulationInfo){
        //Route transformedRoute

        route.setStartDate(transformDateReverse(simulationInfo,route.getStartDate()));
        route.setFinishDate(transformDateReverse(simulationInfo,route.getFinishDate()));

        for(Route.Order o : route.getOrders()){
            o.setDeliveryDate(transformDateReverse(simulationInfo,o.getDeliveryDate()));
        }

        return route;
    }

    public List<Route> findByDateAndMonitoring(LocalDateTime actualDate, boolean monitoring) {
        return routeRepository.findByStartDateBeforeAndFinishDateAfterAndMonitoringAndCancelled(actualDate, actualDate, monitoring, false);
    }

    public LocalDateTime transformDateReverse(SimulationInfo simulationInfo, LocalDateTime dateToConvert){
        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = NANOS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.minusNanos(differenceTransformReal);

        long amountNanos = NANOS.between(simulationStartReal, dateToConvert);
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartReal.toLocalDate(),simulationStartReal.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }

    public LocalDateTime transformDateSpeed(SimulationInfo simulationInfo, int speed, LocalDateTime dateToConvert){
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();
        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        amountNanos /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }


}

