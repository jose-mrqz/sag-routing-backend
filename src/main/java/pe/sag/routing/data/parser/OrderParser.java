package pe.sag.routing.data.parser;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.User;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;
import pe.sag.routing.shared.util.enums.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderParser {
    public static Order fromDto(OrderDto orderDto) {
        Order order = Order.builder()
                .code(orderDto.getCode())
                .demandGLP(orderDto.getDemandGLP())
                .totalDemand(orderDto.getDemandGLP())
                .registrationDate(orderDto.getRegistrationDate())
                .deadlineDate(orderDto.getDeadlineDate())
                .deliveryDate(orderDto.getDeliveryDate())
                //.status(orderDto.getStatus())
                //.monitoring(orderDto.getMonitoring())
                .active(true)
                .build();
        order.setX(orderDto.getX());
        order.setY(orderDto.getY());
        return order;
    }

    public static OrderDto toDto(Order order) {
        OrderDto orderDto = BaseParser.parse(order, OrderDto.class);
        orderDto.setStatus(order.getStatus().toString());
        orderDto.setDemandGLP(order.getDemandGLP());
        return orderDto;
    }
}
