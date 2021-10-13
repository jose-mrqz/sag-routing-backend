package pe.sag.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import pe.sag.routing.core.scheduling.OrderTaskScheduler;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class RoutingApplication {
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));
    }

    public static void main(String[] args) {
        SpringApplication.run(RoutingApplication.class, args);
    }

    @Scheduled(fixedDelay = 15000L)
    void someJob() {
        OrderTaskScheduler orderTaskScheduler = new OrderTaskScheduler();
        orderTaskScheduler.scheduleMessage();
    }

}
