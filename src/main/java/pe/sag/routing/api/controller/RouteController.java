package pe.sag.routing.api.controller;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.api.response.ActiveRouteResponse;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.model.User;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.shared.util.enums.Role;

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
        List<Route> activeRoutes = routeService.getActiveRoutes();
        List<ActiveRouteResponse> payload = new ArrayList<>();
        activeRoutes.forEach(r -> payload.add(ActiveRouteResponse.builder()
                .startDate(r.getStartDate())
                .velocity(13.889)
                //.orders(r.getOrders().stream().map(OrderParser::toDto).collect(Collectors.toList()))
                .route(r.getNodes()).build()));
        RestResponse response = new RestResponse(HttpStatus.OK, payload);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    protected ResponseEntity<?> scheduleRoutes() {
        List<Truck> availableTrucks = truckService.findByAvailable(true);
        List<Order> pendingOrders = orderService.listPendings();
        Planner planner = new Planner(availableTrucks, pendingOrders);
        planner.run();

        List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();
        for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
            if(sr.getTotalTourDistance() == 0) continue;

            Route r = Route.builder()
                    //.truck(//falta)
                    //.orders(sr.getOrders()).stream().map(Order::valueOf).collect(Collectors.toList()))
                    //.nodes(sr.getNodes()).stream().map(Pair::valueOf).collect(Collectors.toList()))
                    .distance(sr.getTotalTourDistance())
                    .fuelConsumed(sr.getTotalFuelConsumption())
                    //.deliveredGLP(sr.get)
                    .startDate(sr.getStartDate())
                    .finishDate(sr.getFinishDate())
                    .active(true)
                    .build();
            routeService.register(r);
        }

        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .body(planner.getSolutionRoutes());
    }
}
