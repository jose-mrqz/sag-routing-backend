package pe.sag.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import pe.sag.routing.core.scheduling.OrderTaskScheduler;

@SpringBootApplication
public class RoutingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingApplication.class, args);
    }

    @Scheduled(fixedDelay = 15000L)
    void someJob() {
        OrderTaskScheduler orderTaskScheduler = new OrderTaskScheduler();
        orderTaskScheduler.scheduleMessage();
    }

}
