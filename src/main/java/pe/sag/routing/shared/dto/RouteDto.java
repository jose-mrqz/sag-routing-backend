package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.core.model.Route;

import javax.validation.constraints.NotBlank;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Order {
        int x;
        int y;
        int indexRoute;
        String deliveryDate;
        String leftDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Node {
        int x;
        int y;
        boolean order;
    }

    @NotBlank
    private String startDate;
    @NotBlank
    private String endDate;
    @NotBlank
    private int timeAttention = 10;
    @NotBlank
    private double velocity = 50;
    @NotBlank
    private String truckCode;
    @NotBlank
    private List<Order> orders;
    @NotBlank
    private List<Node> route;

    public void setOrders(List<Route.Order> orders) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.orders = new ArrayList<>();
        for (Route.Order order : orders) {
            RouteDto.Order newOrder = Order.builder()
                    .x(order.getX())
                    .y(order.getY())
                    .deliveryDate(order.getDeliveryDate().format(format))
                    .leftDate(order.getDeliveryDate().plusMinutes(timeAttention).format(format))
                    .build();
            this.orders.add(newOrder);
        }
    }

    public void setNodes(List<Pair<Integer, Integer>> nodes) {
        this.route = new ArrayList<>();
        for (Pair<Integer, Integer> node : nodes) {
            RouteDto.Node newNode = Node.builder()
                    .x(node.getX())
                    .y(node.getY()).build();
        }
    }
}
