package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.SimulationInfo;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

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
    private int timeAttention = 10*60;
    @NotBlank
    private double velocity = 13.8889;
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
                    .leftDate(order.getDeliveryDate().plusSeconds(timeAttention).format(format))
                    .build();
            this.orders.add(newOrder);
        }
    }

    public void setNodes(List<Pair<Integer, Integer>> nodes) {
        this.route = new ArrayList<>();
        for (Pair<Integer, Integer> node : nodes) {
            boolean isOrder = false;
            for (RouteDto.Order order : this.orders) {
                if (order.getX() == node.getX() && order.getY() == node.getY()) {
                    isOrder = true;
                    break;
                }
            }
            RouteDto.Node newNode = Node.builder()
                    .x(node.getX())
                    .y(node.getY())
                    .order(isOrder)
                    .build();
            this.route.add(newNode);
        }
        int index = 0;
        for (RouteDto.Order order : this.orders) {
            for (; index < this.route.size(); index++) {
                if (order.getY() == route.get(index).getY() &&
                    order.getX() == route.get(index).getX()) {
                    order.setIndexRoute(index);
                    break;
                }
            }
        }
    }

    public String transformDate(SimulationInfo simulationInfo, int speed, String sDateToConvert){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateToConvert = LocalDateTime.parse(sDateToConvert, format);

        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = SECONDS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.plusSeconds(differenceTransformReal);

        long amountSeconds = SECONDS.between(simulationStartTransform, dateToConvert);
        amountSeconds /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusSeconds(amountSeconds);
        return transformedDate.format(format);
    }

    public RouteDto transformRoute(SimulationInfo simulationInfo, int speed){
        RouteDto transformedRoute = RouteDto.builder()
                .startDate(getStartDate())
                .endDate(getEndDate())
                .timeAttention(getTimeAttention())
                .velocity(getVelocity())
                .truckCode(getTruckCode())
                .orders(getOrders())
                .route(getRoute())
                .build();

        transformedRoute.setStartDate(transformDate(simulationInfo,speed,getStartDate()));
        transformedRoute.setEndDate(transformDate(simulationInfo,speed,getEndDate()));
        transformedRoute.setTimeAttention(getTimeAttention()/speed);
        transformedRoute.setVelocity(getVelocity()*speed);

        for(RouteDto.Order o : transformedRoute.getOrders()){
            o.setDeliveryDate(transformDate(simulationInfo,speed,o.getDeliveryDate()));
            o.setLeftDate(transformDate(simulationInfo,speed,o.getLeftDate()));
        }

        return transformedRoute;
    }
}
