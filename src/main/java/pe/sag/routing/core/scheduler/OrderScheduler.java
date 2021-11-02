package pe.sag.routing.core.scheduler;

import lombok.Data;
import org.springframework.stereotype.Component;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Data
public class OrderScheduler {
    public final OrderRepository orderRepository;
    private static HashMap<String, Timer> timerRecord = new HashMap<>();

    public OrderScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void scheduleStatusChange(String id, OrderStatus status, LocalDateTime now) {
        if (now == null) return; //error handle
        Timer currentTimer = timerRecord.getOrDefault(id, null);
        if (currentTimer != null) currentTimer.cancel();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Optional<Order> orderOptional = orderRepository.findById(id);
                if (orderOptional.isPresent()) {
                    Order o = orderOptional.get();
                    o.setStatus(status);
                    orderRepository.save(o);
                }
                timer.cancel();
            }
        };
        long wait = Duration.between(LocalDateTime.now(), now).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
        timerRecord.put(id, timer);
    }
}
