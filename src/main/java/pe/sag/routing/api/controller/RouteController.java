package pe.sag.routing.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.sag.routing.algorithm.Planner;
import pe.sag.routing.api.response.ActiveRouteResponse;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.service.RouteService;
import pe.sag.routing.data.parser.OrderParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/route")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    protected ResponseEntity<?> getActive() {
        List<Route> activeRoutes = routeService.getActiveRoutes();
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
    protected ResponseEntity<?> scheduleRoutes() {
    }
}
