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

import static java.time.temporal.ChronoUnit.MINUTES;

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

    public List<Route> getActiveRoutes(boolean monitoring) {
        return routeRepository.findByStartDateBeforeAndFinishDateAfterAndMonitoring(LocalDateTime.now(), LocalDateTime.now(), monitoring);
    }

    public LocalDateTime transformDate(LocalDateTime simulationStart, int speed, LocalDateTime dateToConvert){
        long amountMinutes = MINUTES.between(simulationStart, dateToConvert);
        amountMinutes /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStart.toLocalDate(),simulationStart.toLocalTime());
        transformedDate = transformedDate.plusMinutes(amountMinutes);
        return transformedDate;
    }

//    public Route transformRoute(Route routeToTransform, LocalDateTime simulationStart, int speed){
//        Route transformedRoute = Route.builder()
//                .truck(routeToTransform.getTruck())
//                .orders(routeToTransform.getOrders())
//                .times(routeToTransform.getTimes())
//                .nodes(routeToTransform.getNodes())
//                .distance(routeToTransform.getDistance())
//                .fuelConsumed(routeToTransform.getFuelConsumed())
//                .deliveredGLP(routeToTransform.getDeliveredGLP())
//                .active(true)
//                .startDate(routeToTransform.getStartDate())
//                .finishDate(routeToTransform.getFinishDate())
//                .build();
//
//        routeToTransform.setStartDate(transformDate(simulationStart,speed,routeToTransform.getStartDate()));
//        routeToTransform.setFinishDate(transformDate(simulationStart,speed,routeToTransform.getFinishDate()));
//
//        for(Order o : routeToTransform.getOrders()){
//            o.setDeliveryDate(transformDate(simulationStart,speed,routeToTransform.getStartDate()));
//            //o.setLeftDate(transformDate(simulationStart,speed,routeToTransform.getLeftDate()));
//        }
//
//        return transformedRoute;
//    }

    public Route getLastRouteByTruckMonitoring(Truck truck) {
        Optional<Route> route = routeRepository.
                findTopByTruckIdAndMonitoringOrderByFinishDateDesc(truck.get_id(), true);
        return route.orElse(null);
    }
}

