package pe.sag.routing.api.controller;

//import org.apache.commons.collections.ArrayStack;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Breakdown;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.SimulationInfo;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.BreakdownService;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.core.service.TruckService;
import pe.sag.routing.data.repository.SimulationInfoRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/breakdown")
public class BreakdownController {
    private final BreakdownService breakdownService;
    private final TruckService truckService;
    private final RouteService routeService;
    private final OrderService orderService;
    private final SimulationInfoRepository simulationInfoRepository;

    public BreakdownController(BreakdownService breakdownService, TruckService truckService, RouteService routeService, OrderService orderService, SimulationInfoRepository simulationInfoRepository) {
        this.breakdownService = breakdownService;
        this.truckService = truckService;
        this.routeService = routeService;
        this.orderService = orderService;
        this.simulationInfoRepository = simulationInfoRepository;
    }

    @GetMapping
    public ResponseEntity<?> getActive() {
        List<Breakdown> activeBreakdowns = breakdownService.getActive();
        RestResponse response = new RestResponse(HttpStatus.OK, activeBreakdowns);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(path = "/simulation")
    public ResponseEntity<?> getSimulation() {
        RestResponse response;
        List<Breakdown> breakdowns = new ArrayList<>();
        List<SimulationInfo> listSimulationInfo = simulationInfoRepository.findAll();
        if (listSimulationInfo.size() != 0) {
            SimulationInfo simulationInfo = listSimulationInfo.get(0);
            RouteController.simulationHelper.getBreakdowns().forEach((k, v) -> breakdowns.add(new Breakdown(v)));
            for (int i = 0; i < breakdowns.size(); i++) {
                Breakdown b = breakdowns.get(i);
                LocalDateTime time = b.getStartDate();
                time = routeService.transformDate(simulationInfo, time);
                time = routeService.transformDateSpeed(simulationInfo, RouteController.simulationSpeed, time);
                b.setStartDate(time);
                LocalDateTime endTime = b.getEndDate();
                endTime = routeService.transformDate(simulationInfo, endTime);
                endTime = routeService.transformDateSpeed(simulationInfo, RouteController.simulationSpeed, endTime);
                b.setEndDate(endTime);
            }
            response = new RestResponse(HttpStatus.OK, breakdowns);
        } else {
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Simulacion no iniciada");
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestParam String truckCode) {
        LocalDateTime now = LocalDateTime.now();
        Truck truck = truckService.findByCodeAndMonitoring(truckCode, true);
        RestResponse response;
        if (truck == null) {
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Codigo de camion incorrecto.");
        } else {
            Route currentRoute = routeService.getCurrentByTruckId(truck.get_id(), true);
            if (currentRoute == null) {
                response = new RestResponse(HttpStatus.BAD_REQUEST, "El camion no se encuentra en ruta en este momento.");
                List<Route> toCancel = routeService.getRoutesAfter(truck.get_id(), LocalDateTime.now());
                cancelRoutes(toCancel);
            } else {
                List<Route.Order> routeOrders = currentRoute.getOrders();
                Route.Order nextOrder = routeOrders.stream()
                        .filter(o -> o.getDeliveryDate().isAfter(now))
                        .findFirst()
                        .orElse(null);
                int traveledNodes = (int) (Duration.between(currentRoute.getStartDate(), now).toSeconds() / 0b1001000); // wtf
                if (traveledNodes < 0) traveledNodes = 0;
                if (traveledNodes >= currentRoute.getNodes().size()) traveledNodes = currentRoute.getNodes().size()-1;
                Breakdown breakdown = Breakdown.builder()
                        .x(currentRoute.getNodes().get(traveledNodes).getX())
                        .y(currentRoute.getNodes().get(traveledNodes).getY())
                        .routeId(currentRoute.get_id())
                        .truckCode(truck.getCode())
                        .startDate(now)
                        .endDate(now.plusMinutes(60))
                        .build();
                currentRoute.setCancelled(true); // cancel current route
                routeService.save(currentRoute);
                truckService.registerBreakdown(truck, now);
                if (nextOrder != null) { // cancel not delivered
                    List<Route.Order> pendingOrders = routeOrders.subList(routeOrders.indexOf(nextOrder), routeOrders.size());
                    int nCancelled = 0;
                    List<Breakdown.Order> cancelledOrders = new ArrayList<>();
                    for (Route.Order order : pendingOrders) {
                        orderService.cancelOrder(order.get_id(), order.getDeliveredGlp());
                        nCancelled++;

                        cancelledOrders.add(new Breakdown.Order(
                                order.get_id(), order.getX(), order.getY(), order.getDeliveryDate(), order.getDeadlineDate(), order.getDeliveredGlp()
                        ));
                    }
                    breakdown.setOrders(cancelledOrders);
                    response = new RestResponse(HttpStatus.OK, "Se registro la averia correctamente, se cancelaron: " + nCancelled + " pedidos de la ruta actual.");
                } else {
                    response = new RestResponse(HttpStatus.OK, "Se registro la averia correctamente, pedidos de la ruta completados de la ruta actual.");
                }
                breakdownService.save(breakdown);
                List<Route> toCancel = routeService.getRoutesAfter(truck.get_id(), currentRoute.getStartDate().plusMinutes(1));
                cancelRoutes(toCancel);
            }
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private void cancelRoutes(List<Route> toCancel) {
        for (Route r : toCancel) {
            for (Route.Order order : r.getOrders()) {
                orderService.cancelOrder(order.get_id(), order.getDeliveredGlp());
            }
            r.setCancelled(true);
            routeService.save(r);
        }
    }
}
