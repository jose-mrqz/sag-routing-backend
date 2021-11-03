package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.scheduler.OrderScheduler;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderScheduler orderScheduler;

    public Order register(OrderDto orderRequest, boolean monitoring) throws IllegalAccessException {
        Order order = OrderParser.fromDto(orderRequest);

        //asign code
        Order lastOrder = findFirstByOrderByCodeDesc();
        int code = 0;
        if (lastOrder == null) code++;
        else code = lastOrder.getCode()+1;
        order.setCode(code);
        order.setTotalDemand(order.getDemandGLP());

        order.setStatus(OrderStatus.PENDIENTE);
        order.setMonitoring(monitoring);
        order.setDeliveryDate(null);
        return orderRepository.save(order);
    }

    public List<Order> registerAll(ArrayList<OrderDto> orderRequest, boolean monitoring) throws IllegalAccessException {
        ArrayList<Order> orders = new ArrayList<>();
        for(OrderDto od : orderRequest){
            Order order = OrderParser.fromDto(od);

            //asign code
            Order lastOrder = findFirstByOrderByCodeDesc();
            int code = 0;
            if (lastOrder == null) code++;
            else code = lastOrder.getCode()+1;
            order.setCode(code);

            order.setStatus(OrderStatus.PENDIENTE);
            order.setMonitoring(monitoring);
            order.setDeliveryDate(null);

            orders.add(order);
        }
        return orderRepository.saveAll(orders);
    }

    public void updateStatus(Order order, OrderStatus status){
        order.setStatus(status);
        if (status == OrderStatus.PROGRAMADO) order.setDemandGLP(0.0);
        orderRepository.save(order);
    }

    public List<OrderDto> list() {
        return orderRepository.findByMonitoringOrderByCodeAsc(true).stream().map(OrderParser::toDto).collect(Collectors.toList());
    }

    public List<Order> listPendingsMonitoring(boolean monitoring) {
        return orderRepository.findByStatusAndMonitoringOrderByRegistrationDateAsc(OrderStatus.PENDIENTE,monitoring);
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

    public List<Order> saveMany(List<OrderDto> ordersDto) {
        List<Order> orders = ordersDto.stream().map(OrderParser::fromDto).collect(Collectors.toList());
        return orderRepository.saveAll(orders);
    }

    public void deleteByMonitoring(boolean monitoring) {
        orderRepository.deleteByMonitoring(monitoring);
    }

    public void scheduleStatusChange(String id, OrderStatus status, LocalDateTime now) {
        orderScheduler.scheduleStatusChange(id, status, now);
    }

    public List<Order> getBatchedByStatusMonitoring(OrderStatus status, boolean isMonitoring) {
        return orderRepository.findFirst200ByStatusAndMonitoringOrderByRegistrationDateAsc(status, isMonitoring);
    }

    public Order cancelOrder(String id, double amount) {
        Optional<Order> orderOptional = orderRepository.findBy_id(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setDemandGLP(amount + order.getDemandGLP());
            orderScheduler.cancelStatusChange(id);
            order.setStatus(OrderStatus.PENDIENTE);
            order.setDeliveryDate(null);
            return orderRepository.save(order);
        } else return null;
    }
}
