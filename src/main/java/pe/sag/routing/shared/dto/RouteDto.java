package pe.sag.routing.shared.dto;

import lombok.*;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.core.model.Route;
import pe.sag.routing.core.model.SimulationInfo;

import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;

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
        LocalDateTime deliveryDate;
        LocalDateTime leftDate;
        LocalDateTime deadlineDate;
        double delivered;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Depot {
        int x;
        int y;
        double refilledGlp;
        LocalDateTime refillDate;
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
    private LocalDateTime startDate;
    @NotBlank
    private LocalDateTime endDate;
    @NotBlank
    private int timeAttention = 10*60;
    @NotBlank
    private double velocity = 250.0/18;
    @NotBlank
    private String truckCode;
    @NotBlank
    private List<Order> orders;
    @NotBlank
    private List<Depot> depots;
    @NotBlank
    private List<Node> route;

    public void setOrders(List<Route.Order> orders) {
        this.orders = new ArrayList<>();
        for (Route.Order order : orders) {
            RouteDto.Order newOrder = Order.builder()
                    .x(order.getX())
                    .y(order.getY())
                    .deliveryDate(order.getDeliveryDate())
                    .leftDate(order.getDeliveryDate().plusSeconds(timeAttention))
                    .deadlineDate(order.getDeadlineDate())
                    .delivered(order.getDeliveredGlp())
                    .build();
            this.orders.add(newOrder);
        }
    }

    public void setDepots(List<Route.Depot> depots) {
        this.depots = new ArrayList<>();
        for (Route.Depot depot : depots) {
            RouteDto.Depot newDepot = Depot.builder()
                    .x(depot.getX())
                    .y(depot.getY())
                    .refillDate(depot.getRefillDate())
                    .refilledGlp(depot.getRefilledGlp())
                    .build();
            this.depots.add(newDepot);
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
//        Collections.reverse(this.route);
//        int index = 0;
//        for (RouteDto.Order order : this.orders) {
//            for (; index < this.route.size(); index++) {
//                if (order.getY() == route.get(index).getY() &&
//                    order.getX() == route.get(index).getX()) {
//                    order.setIndexRoute(index);
//                    index++;
//                    break;
//                }
//            }
//        }
        int index = 0;
        LocalDateTime start = this.getStartDate();
        Order order;
        LocalDateTime curr;
        for (int i = 0; i < this.orders.size(); i++) {
            order = this.orders.get(i);
            curr = order.getDeliveryDate();
            long seconds = Duration.between(start, curr).toSeconds();
            index += seconds/72;
            start = order.getLeftDate();
            if (index >= this.route.size()-4) index = this.route.size()-1;
            if (i != this.orders.size()-1) {
                Order nextOrder = this.orders.get(i+1);
                if (nextOrder.getX() == order.getX() &&
                    nextOrder.getY() == order.getY() &&
                    nextOrder.getDeliveryDate().isAfter(order.getDeliveryDate().plusMinutes(7)) &&
                    nextOrder.getDeliveryDate().isBefore(order.getDeliveryDate().plusMinutes(13))) {
                    nextOrder.setIndexRoute(index);
                    start = start.plusMinutes(10);
                    i++;
                    continue;
                }
            }
            order.setIndexRoute(index);
        }

    }

    public LocalDateTime transformDateSpeed(SimulationInfo simulationInfo, int speed, LocalDateTime dateToConvert){
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();
        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        amountNanos /= speed;
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }

    public RouteDto transformRouteSpeed(SimulationInfo simulationInfo, int speed){
        List<RouteDto.Order> orders = new ArrayList<>();
        for(RouteDto.Order o : getOrders()){
            RouteDto.Order newOrder = new RouteDto.Order(o.getX(), o.getY(), o.getIndexRoute(),
                    o.getDeliveryDate(), o.getLeftDate(), o.getDeadlineDate(), o.getDelivered());
            orders.add(newOrder);
        }

        RouteDto transformedRoute = RouteDto.builder()
                .startDate(getStartDate())
                .endDate(getEndDate())
                .timeAttention(getTimeAttention())
                .velocity(getVelocity())
                .truckCode(getTruckCode())
                .orders(orders)
                .route(getRoute())
                .build();

        transformedRoute.setStartDate(transformDateSpeed(simulationInfo,speed,getStartDate()));
        transformedRoute.setEndDate(transformDateSpeed(simulationInfo,speed,getEndDate()));
        transformedRoute.setTimeAttention(getTimeAttention()/speed);
        transformedRoute.setVelocity(getVelocity()*speed);

        for(RouteDto.Order o : transformedRoute.getOrders()){
            o.setDeliveryDate(transformDateSpeed(simulationInfo,speed,o.getDeliveryDate()));
            o.setLeftDate(transformDateSpeed(simulationInfo,speed,o.getLeftDate()));
            o.setDeadlineDate(transformDateSpeed(simulationInfo,speed,o.getDeadlineDate()));
        }

        return transformedRoute;
    }

    public LocalDateTime transformDate(SimulationInfo simulationInfo, LocalDateTime dateToConvert){
        LocalDateTime simulationStartReal = simulationInfo.getStartDateReal();
        LocalDateTime simulationStartTransform = simulationInfo.getStartDateTransformed();

        long differenceTransformReal = NANOS.between(simulationStartReal, simulationStartTransform);
        dateToConvert = dateToConvert.plusNanos(differenceTransformReal);

        long amountNanos = NANOS.between(simulationStartTransform, dateToConvert);
        LocalDateTime transformedDate = LocalDateTime.of(simulationStartTransform.toLocalDate(),simulationStartTransform.toLocalTime());
        transformedDate = transformedDate.plusNanos(amountNanos);
        return transformedDate;
    }

    public RouteDto transformRoute(SimulationInfo simulationInfo){
        List<RouteDto.Order> orders = new ArrayList<>();
        for(RouteDto.Order o : getOrders()){
            RouteDto.Order newOrder = new RouteDto.Order(o.getX(), o.getY(), o.getIndexRoute(),
                    o.getDeliveryDate(), o.getLeftDate(), o.getDeadlineDate(), o.getDelivered());
            orders.add(newOrder);
        }

        RouteDto transformedRoute = RouteDto.builder()
                .startDate(getStartDate())
                .endDate(getEndDate())
                .timeAttention(getTimeAttention())
                .velocity(getVelocity())
                .truckCode(getTruckCode())
                .orders(orders)
                .route(getRoute())
                .build();

        transformedRoute.setStartDate(transformDate(simulationInfo,getStartDate()));
        transformedRoute.setEndDate(transformDate(simulationInfo,getEndDate()));

        for(RouteDto.Order o : transformedRoute.getOrders()){
            o.setDeliveryDate(transformDate(simulationInfo,o.getDeliveryDate()));
            o.setLeftDate(transformDate(simulationInfo,o.getLeftDate()));
            o.setDeadlineDate(transformDate(simulationInfo,o.getDeadlineDate()));
        }

        return transformedRoute;
    }

    public boolean inDateRange(LocalDateTime filterDate){
        LocalDateTime filterDateRangeStart = filterDate.minusDays(1);
        LocalDateTime filterDateRangeEnd = filterDate.plusDays(1);
        return !filterDateRangeEnd.isBefore(startDate) && !endDate.isBefore(filterDateRangeStart);
    }
}
