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

    public LocalDateTime transformDate(LocalDateTime simulationStart, int speed, LocalDateTime dateToConvert){
        long amountSeconds = SECONDS.between(simulationStart, dateToConvert);
        amountSeconds /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStart.toLocalDate(),simulationStart.toLocalTime());
        transformedDate = transformedDate.plusSeconds(amountSeconds);
        return transformedDate;
    }

    public Route transformRoute(Route routeToTransform, LocalDateTime simulationStart, int speed){
        Route transformedRoute = Route.builder()
                .truckId(routeToTransform.getTruckId())
                .orders(routeToTransform.getOrders())
                .nodes(routeToTransform.getNodes())
                .fuelConsumed(routeToTransform.getFuelConsumed())
                .deliveredGLP(routeToTransform.getDeliveredGLP())
                .active(true)
                .startDate(routeToTransform.getStartDate())
                .finishDate(routeToTransform.getFinishDate())
                .monitoring(false)
                .build();

        transformedRoute.setStartDate(transformDate(simulationStart,speed,routeToTransform.getStartDate()));
        transformedRoute.setFinishDate(transformDate(simulationStart,speed,routeToTransform.getFinishDate()));

        for(Route.Order o : routeToTransform.getOrders()){
            o.setDeliveryDate(transformDate(simulationStart,speed,routeToTransform.getStartDate()));
            //o.setLeftDate(transformDate(simulationStart,speed,routeToTransform.getLeftDate()));
        }

        return transformedRoute;
    }

    public Route getLastRouteByTruckMonitoring(Truck truck) {
        Optional<Route> route = routeRepository.
                findTopByTruckIdAndMonitoringOrderByFinishDateDesc(truck.get_id(), true);
        return route.orElse(null);
    }
}

