package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Breakdown;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.core.service.BreakdownService;
import pe.sag.routing.core.service.OrderService;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.core.service.TruckService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/breakdown")
public class BreakdownController {
    private final BreakdownService breakdownService;
    private final TruckService truckService;
    private final RouteService routeService;
    private final OrderService orderService;

    public BreakdownController(BreakdownService breakdownService, TruckService truckService, RouteService routeService, OrderService orderService) {
        this.breakdownService = breakdownService;
        this.truckService = truckService;
        this.routeService = routeService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> getActive() {
        List<Breakdown> activeBreakdowns = breakdownService.getActive();
        RestResponse response = new RestResponse(HttpStatus.OK, activeBreakdowns);
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
                        .y(currentRoute.getNodes().get(traveledNodes).getX())
                        .routeId(currentRoute.get_id())
                        .truckCode(truck.getCode())
                        .startDate(now)
                        .endDate(now.plusMinutes(60))
                        .build();
                currentRoute.setCancelled(true); // cancel current route
                routeService.save(currentRoute);
                breakdownService.save(breakdown);
                truckService.registerBreakdown(truck, now);
                if (nextOrder != null) { // cancel not delivered
                    List<Route.Order> pendingOrders = routeOrders.subList(routeOrders.indexOf(nextOrder), routeOrders.size());
                    int nCancelled = 0;
                    for (Route.Order order : pendingOrders) {
                        orderService.cancelOrder(order.get_id(), order.getDeliveredGlp());
                        nCancelled++;
                    }
                    response = new RestResponse(HttpStatus.OK, "Se registro la averia correctamente, se cancelaron: " + nCancelled + " pedidos.");
                } else {
                    response = new RestResponse(HttpStatus.OK, "Se registro la averia correctamente, pedidos de la ruta completados.");
                }
            }
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
