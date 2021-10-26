package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.api.request.SimulationRequest;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.api.response.SimulationResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.data.parser.RouteParser;
import pe.sag.routing.data.repository.SimulationInfoRepository;
import pe.sag.routing.shared.dto.RouteDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/route")
public class RouteController {
    @Autowired
    private RouteService routeService;
    @Autowired
    private TruckService truckService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SimulationInfoRepository simulationInfoRepository;

    @GetMapping
    protected ResponseEntity<?> getActive() {
        List<Route> activeRoutes = routeService.getActiveRoutes(LocalDateTime.now(),true);
        List<RouteDto> routesDto = activeRoutes.stream().map(RouteParser::toDto).collect(Collectors.toList());
        RestResponse response = new RestResponse(HttpStatus.OK, routesDto);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/simulation")
    protected ResponseEntity<?> getActiveSimulation(@RequestBody SimulationRequest request) {
        List<Route> activeRoutes = routeService.findByMonitoring(false);
        List<RouteDto> routesDto = activeRoutes.stream().map(RouteParser::toDto).collect(Collectors.toList());
        ArrayList<RouteDto> routesTransformedDto = new ArrayList<>();

        //sacar simulation info de bd
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        SimulationInfo simulationInfo = listSimulationInfo.get(0);

        for(RouteDto r : routesDto){
            RouteDto rt = r.transformRoute(simulationInfo,request.getSpeed());
            routesTransformedDto.add(rt);
        }

        SimulationResponse simulationResponse = new SimulationResponse(routesDto, routesTransformedDto);
        RestResponse response = new RestResponse(HttpStatus.OK, simulationResponse);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    public ResponseEntity<?> scheduleRoutes() {
        List<Truck> availableTrucks = truckService.findByAvailableAndMonitoring(true, true);
        List<Order> pendingOrders = orderService.listPendingsMonitoring(true);

        for (Truck truck : availableTrucks) {
            Route lastRoute = routeService.getLastRouteByTruckMonitoring(truck, true);
            if (lastRoute != null) truck.setLastRouteEndTime(lastRoute.getFinishDate());
            else truck.setLastRouteEndTime(LocalDateTime.now());
        }

        //mejorar con formato de error: colapso logistico
        if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
            Planner planner = new Planner(availableTrucks, pendingOrders);
            planner.run();
            List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
            List<Pair<String, LocalDateTime>> solutionOrders = planner.getSolutionOrders();

            for(Order o : pendingOrders){
                boolean scheduled = false;
                for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                    //if(delivery.getY() == null) //muere

                    if (delivery.getX().equals(o.get_id()) && delivery.getY() != null) {
                        scheduled = true;
                        break;
                    }
                }
                if (scheduled) orderService.updateStatus(o, OrderStatus.IN_PROGRESS);
            }
            for (Pair<String,LocalDateTime> delivery : solutionOrders) {
                orderService.scheduleStatusChange(delivery.getX(), OrderStatus.COMPLETED, delivery.getY());
            }
            for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
                Route r = new Route(sr);
                routeService.register(r);
            }
        }
        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo de Monitoreo realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PostMapping(path = "/simulationAlgorithm")
    public ResponseEntity<?> scheduleRoutesSimulation(LocalDateTime startDateReal) {
        routeService.deleteByMonitoring(false);
        truckService.updateAvailablesSimulation();

        List<Truck> availableTrucks = truckService.findByAvailableAndMonitoring(true, false);
        List<Order> pendingOrders = orderService.listPendingsMonitoring(false);

        for (Truck truck : availableTrucks) {
            Route lastRoute = routeService.getLastRouteByTruckMonitoring(truck, false);
            if (lastRoute != null) truck.setLastRouteEndTime(lastRoute.getFinishDate());
            else truck.setLastRouteEndTime(startDateReal);
        }

        if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
            Planner planner = new Planner(availableTrucks, pendingOrders);
            planner.run();
            List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
            List<Pair<String, LocalDateTime>> solutionOrders = planner.getSolutionOrders();

            for(Order o : pendingOrders){
                boolean scheduled = false;
                for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                    if (delivery.getX().equals(o.get_id()) && delivery.getY() != null) {
                        scheduled = true;
                        break;
                    }
                }
                if (scheduled) orderService.updateStatus(o, OrderStatus.IN_PROGRESS);
            }
            for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
                Route r = new Route(sr);
                r.setMonitoring(false);
                routeService.register(r);
            }
        }
        //estadisticas de simulacion (mostrar en front?)
        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo de Simulacion realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
