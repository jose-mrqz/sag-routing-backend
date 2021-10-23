package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Truck;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order register(OrderDto orderRequest, boolean monitoring) throws IllegalAccessException {
        Order order = OrderParser.fromDto(orderRequest);

        //asign code
        Order lastOrder = findFirstByOrderByCodeDesc();
        int code = 0;
        if (lastOrder == null) code++;
        else code = lastOrder.getCode()+1;
        order.setCode(code);

        order.setStatus(OrderStatus.PENDING);
        order.setMonitoring(monitoring);
        order.setDeliveryDate(null);
        return orderRepository.save(order);
    }

    public Order updateStatus(Order order, OrderStatus status){
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<OrderDto> list() {
        return orderRepository.findByMonitoringOrderByCodeAsc(true).stream().map(OrderParser::toDto).collect(Collectors.toList());
    }

    public List<Order> listPendingsMonitoring(boolean monitoring) {
        return orderRepository.findByStatusAndMonitoring(OrderStatus.PENDING,monitoring);
    }

    public Order findByCode(String code) throws IllegalAccessException {
        return orderRepository.findByCode(code).orElseThrow(IllegalAccessException::new);
    }

    public Order findFirstByOrderByCodeDesc() throws IllegalAccessException {
        Optional<Order> lastOrder = orderRepository.findFirstByOrderByCodeDesc();
        return lastOrder.orElse(null);
    }

    public Order findById(String id) {
        Optional<Order> order = orderRepository.findBy_id(id);
        return order.orElse(null);
    }

    public void scheduleStatusChange(String id, OrderStatus status, LocalDateTime now) {
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
    }
}
