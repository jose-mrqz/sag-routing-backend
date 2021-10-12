package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order register(OrderDto orderRequest) throws IllegalAccessException {
        Order order = OrderParser.fromDto(orderRequest);

        //asign code
        Order lastOrder = findFirstByOrderByCodeDesc();
        order.setCode(lastOrder.getCode()+1);

        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryDate(null);
        return orderRepository.save(order);
    }

    public List<OrderDto> list() {
        return orderRepository.findAll(Sort.by(Sort.Direction.ASC,"code")).stream().map(OrderParser::toDto).collect(Collectors.toList());
    }

    public List<OrderDto> listPendings() {
        return orderRepository.findByStatus(OrderStatus.PENDING).stream().map(OrderParser::toDto).collect(Collectors.toList());
    }

    public Order findByCode(String code) throws IllegalAccessException {
        return orderRepository.findByCode(code).orElseThrow(IllegalAccessException::new);
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order findFirstByOrderByCodeDesc() throws IllegalAccessException {
        return orderRepository.findFirstByOrderByCodeDesc().orElseThrow(IllegalAccessException::new);
    }
}
