package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.*;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.DepotRepository;
import pe.sag.routing.data.repository.RouteRepository;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.*;

@Service
public class RouteService {
    private final RouteRepository routeRepository;
    private final DepotRepository depotRepository;

    public RouteService(RouteRepository routeRepository, DepotRepository depotRepository) {
        this.routeRepository = routeRepository;
        this.depotRepository = depotRepository;
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

    public void deleteList(List<Route> toDelete) {
        routeRepository.deleteAll(toDelete);
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
            o.setDeadlineDate(transformDate(simulationInfo,o.getDeadlineDate()));
        }

        return route;
    }

    public Route transformRouteReverse(Route route, SimulationInfo simulationInfo){
        //Route transformedRoute

        route.setStartDate(transformDateReverse(simulationInfo,route.getStartDate()));
        route.setFinishDate(transformDateReverse(simulationInfo,route.getFinishDate()));

        for(Route.Order o : route.getOrders()){
            o.setDeliveryDate(transformDateReverse(simulationInfo,o.getDeliveryDate()));
            o.setDeadlineDate(transformDateReverse(simulationInfo,o.getDeadlineDate()));
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

    public LocalDateTime transformDateReverseSpeed(SimulationInfo simulationInfo, int speed, LocalDateTime dateToConvert){
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();
        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        amountNanos *= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
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

    public List<FuelConsume> getFuelConsumedPerDay(LocalDateTime startDate, LocalDateTime endDate){
        List<FuelConsume> fuelConsumes = new ArrayList<>();
        List<Route> routes = routeRepository.findByFinishDateBetweenAndMonitoringAndCancelled(startDate, endDate.plusDays(1), true, false);
        LocalDateTime actualDate = LocalDateTime.of(startDate.toLocalDate(),startDate.toLocalTime());

        double totalFuel = 0;
        for(Route r : routes){
            //revisar condicion
            while(actualDate.toLocalDate().isBefore(r.getFinishDate().toLocalDate())){
                if(totalFuel > 0) fuelConsumes.add(new FuelConsume(actualDate, totalFuel));
                totalFuel = 0;
                actualDate = actualDate.plusDays(1);
            }
            totalFuel += r.getFuelConsumed();
        }
        if(totalFuel > 0) fuelConsumes.add(new FuelConsume(actualDate, totalFuel));
        return fuelConsumes;
    }

    public List<GlpRefill> getGlpRefilledPerDay(LocalDateTime startDate, LocalDateTime endDate){
        List<GlpRefill> glpRefills = new ArrayList<>();
        List<Depot> depots = depotRepository.findAll();
        List<Route> routes = routeRepository.findByFinishDateBetweenAndMonitoringAndCancelled(startDate, endDate.plusDays(1), true, false);

        String depotName;
        for(Route r : routes){
            if(r.getDepots().size() > 0){
                for(Route.Depot d : r.getDepots()){
                    if(d.getX() == depots.get(0).getX()) depotName = depots.get(0).getName();
                    else depotName = depots.get(1).getName();

                    glpRefills.add(new GlpRefill(d.getRefillDate(), depotName, d.getRefilledGlp(), r.getTruckCode()));
                }
            }
        }
        return glpRefills;
    }

    public List<RouteDto> getLastRoutesColapse(List<RouteDto> routesReal){
        boolean truckCodesRepeated;
        List<String> truckCodes = new ArrayList<>();
        List<RouteDto> routesFiltered = new ArrayList<>();
        for(RouteDto r : routesReal){
            truckCodesRepeated = false;
            for(String tc : truckCodes){
                if(tc.compareTo(r.getTruckCode()) == 0){
                    truckCodesRepeated = true;
                    break;
                }
            }
            if(truckCodesRepeated){
                //quitar duplicado
                for(RouteDto rf : routesFiltered){
                    if(rf.getTruckCode().compareTo(r.getTruckCode()) == 0){
                        routesFiltered.remove(rf);
                        break;
                    }
                }
            }
            truckCodes.add(r.getTruckCode());
            routesFiltered.add(r);
        }
        return routesFiltered;
    }

    public List<Route> getRoutesAfter(String truckId, LocalDateTime time) {
        return routeRepository.findByTruckIdAndStartDateAfterAndCancelled(truckId, time.plusMinutes(1), false);
    }
}

