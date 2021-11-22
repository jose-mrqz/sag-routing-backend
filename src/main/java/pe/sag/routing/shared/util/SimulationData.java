package pe.sag.routing.shared.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.algorithm.Order;
import pe.sag.routing.core.model.SimulationInfo;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationData {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Order {
        private int x;
        private int y;
        private String _id;
        private LocalDateTime registrationDate;
        private LocalDateTime registrationDateTransformed;
        private LocalDateTime deadlineDate;
        private double demand;
    }

    private int nOrders;
    private int nScheduled;
    private String message;
    private boolean finished;
    private Order lastOrder;
    private LocalDateTime lastRouteEndTime = null;

    public void setOrder(pe.sag.routing.algorithm.Order order, LocalDateTime transformedDate) {
        this.lastOrder = Order.builder()
                .x(order.getX())
                .y(order.getY())
                ._id(order.get_id())
                .registrationDate(order.getTwOpen())
                .registrationDateTransformed(transformedDate)
                .deadlineDate(order.getTwClose())
                .demand(order.getTotalDemand())
                .build();
    }

    public void setOrderDateTransformed(LocalDateTime transformed) {
        this.lastOrder.setRegistrationDateTransformed(transformed);
    }
}
