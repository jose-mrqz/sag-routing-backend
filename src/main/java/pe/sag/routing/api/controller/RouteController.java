package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.algorithm.Node;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.api.response.ActiveRouteResponse;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/route")
public class RouteController {
    private final RouteService routeService;
    private final TruckService truckService;
    private final OrderService orderService;

    public RouteController(RouteService routeService, TruckService truckService, OrderService orderService) {
        this.routeService = routeService;
        this.truckService = truckService;
        this.orderService = orderService;
    }

    @GetMapping
    protected ResponseEntity<?> getActive() {
//        List<Route> activeRoutes = routeService.getActiveRoutes();
        List<Route> activeRoutes = routeService.getAll();
        List<ActiveRouteResponse> payload = new ArrayList<>();
        activeRoutes.forEach(r -> payload.add(ActiveRouteResponse.builder()
                .startDate(r.getStartDate())
                .velocity(13.889)
                .orders(r.getOrders().stream().map(OrderParser::toDto).collect(Collectors.toList()))
                .route(r.getNodes()).build()));
        RestResponse response = new RestResponse(HttpStatus.OK, payload);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    protected ResponseEntity<?> scheduleRoutes() throws IllegalAccessException {
        List<Truck> availableTrucks = truckService.findByAvailable(true);
        List<Order> pendingOrders = orderService.listPendings();
        Planner planner = new Planner(availableTrucks, pendingOrders);
        planner.run();
        List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();

        for(int i=0;i< availableTrucks.size();i++){
            pe.sag.routing.algorithm.Route sr = solutionRoutes.get(i);
            if(sr.getTotalTourDistance() == 0) continue;
            truckService.updateAvailable(availableTrucks.get(i),false);
        }
        for(Order o : pendingOrders){
            orderService.updateStatus(o,OrderStatus.IN_PROGRESS);
        }

        for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
            if(sr.getTotalTourDistance() == 0) continue;
            ArrayList<Order> orders = new ArrayList<>();
            for (Node n : sr.getNodes()) {
                if (n instanceof pe.sag.routing.algorithm.Order) {
                    orders.add(orderService.findById(((pe.sag.routing.algorithm.Order)n).get_id()));
                }
            }
            sr.generatePath();
            Route r = Route.builder()
                    .truck(truckService.findById(sr.getTruckId()))
                    .orders(orders)
                    .nodes(sr.getPath())
                    .distance(sr.getTotalTourDistance())
                    .fuelConsumed(sr.getTotalFuelConsumption())
                    .deliveredGLP(sr.getTotalDelivered())
                    .startDate(sr.getStartDate())
                    .finishDate(sr.getFinishDate())
                    .times(sr.getTimes())
                    .active(true)
                    .build();
            routeService.register(r);
        }
        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
