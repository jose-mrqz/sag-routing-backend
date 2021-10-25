package pe.sag.routing.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.sag.routing.api.controller.RouteController;

import java.time.LocalDateTime;

@Component
public class RouteScheduler {
    private final RouteController routeController;

    public RouteScheduler(RouteController routeController) {
        this.routeController = routeController;
    }

    @Scheduled(initialDelayString = "PT1M",fixedDelayString = "PT15M")
    void schedulePendingOrders() {
        System.out.println("Scheduling @: " + LocalDateTime.now());
        routeController.scheduleRoutes();
    }
}
