package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Order;
import pe.sag.routing.shared.dto.OrderDto;

public class OrderParser {
    public static Order fromDto(OrderDto orderDto) {
        return BaseParser.parse(orderDto, Order.class);
    }

    public static OrderDto toDto(Order order) {
        OrderDto orderDto = BaseParser.parse(order, OrderDto.class);
        orderDto.setStatus(order.getStatus().toString());
        orderDto.setDemandGLP(order.getTotalDemand());
        return orderDto;
    }
}
