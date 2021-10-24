package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.algorithm.Node;
import pe.sag.routing.algorithm.NodeInfo;
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
        List<Route> activeRoutes = routeService.getAll();
        RestResponse response = new RestResponse(HttpStatus.OK, activeRoutes);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping
    protected ResponseEntity<?> scheduleRoutes() throws IllegalAccessException {
        List<Truck> availableTrucks = truckService.findByAvailable(true);
        List<Order> pendingOrders = orderService.listPendings();

        if (pendingOrders.size() != 0 && availableTrucks.size() != 0) {
            Planner planner = new Planner(availableTrucks, pendingOrders);
            planner.run();
            List<pe.sag.routing.algorithm.Route> solutionRoutes = planner.getSolutionRoutes();

//            for(int i = 0; i < availableTrucks.size(); i++){
//                pe.sag.routing.algorithm.Route sr = solutionRoutes.get(i);
//                if(sr.getTotalTourDistance() == 0) continue;
//                truckService.updateAvailable(availableTrucks.get(i),false);
//                truckService.scheduleStatusChange(availableTrucks.get(i), true, sr.getFinishDate());
//            }
//            for(Order o : pendingOrders){
//                orderService.updateStatus(o,OrderStatus.IN_PROGRESS);
//            }

            for(pe.sag.routing.algorithm.Route sr : solutionRoutes){
                ArrayList<Order> orders = new ArrayList<>();
//                for (NodeInfo n : sr.getNodesInfo()) {
//                    if (n instanceof pe.sag.routing.algorithm.OrderInfo) {
//                        orderService.scheduleStatusChange(((pe.sag.routing.algorithm.Order)n).get_id(),
//                                OrderStatus.COMPLETED, ((pe.sag.routing.algorithm.Order)n).getDeliveryTime());
//                    }
//                }
                Route r = new Route(sr);
                routeService.register(r);
            }
        }
        RestResponse response = new RestResponse(HttpStatus.OK, "Algoritmo realizado correctamente.");
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
