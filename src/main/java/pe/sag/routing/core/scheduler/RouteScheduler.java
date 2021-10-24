package pe.sag.routing.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.sag.routing.api.controller.RouteController;

@Component
public class RouteScheduler {
    private final RouteController routeController;

    public RouteScheduler(RouteController routeController) {
        this.routeController = routeController;
    }

    @Scheduled(fixedDelayString = "PT15M")
    void someJob() {
        routeController.scheduleRoutes();
    }
}
