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
        int indexRoute;
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
        boolean depot;
        double glp;
        /*public boolean isDepot(){
            return (x == 12 && y == 8) || (x == 42 && y == 42) || (x == 63 && y == 3);
        }*/
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
    private double truckGlpCapacity;
    @NotBlank
    private List<Order> orders;
    @NotBlank
    private List<Depot> depots;
    @NotBlank
    private List<Node> route;
    private List<Node> cornerNodes;

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
                    .order(false)
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
            order.setIndexRoute(index);
            if (index < this.getRoute().size())
                this.getRoute().get(index).setOrder(true);
            if (index >= this.route.size()-4) index = this.route.size()-1;
            if (i != this.orders.size()-1) {
                Order nextOrder = this.orders.get(i+1);
                if (nextOrder.getX() == order.getX() &&
                    nextOrder.getY() == order.getY() &&
                    nextOrder.getDeliveryDate().isAfter(order.getDeliveryDate().plusMinutes(7)) &&
                    nextOrder.getDeliveryDate().isBefore(order.getDeliveryDate().plusMinutes(13))) {
                    nextOrder.setIndexRoute(index);
                    if (index < this.getRoute().size())
                        this.getRoute().get(index).setOrder(true);
                    start = start.plusMinutes(10);
                    i++;
//                    continue;
                }
            }
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

    public void generateCornerNodes(){
        //identificar depots con index
        this.getRoute().get(0).setDepot(true);
        if(this.getRoute().get(this.getRoute().size()-1).x == 12 &&
                this.getRoute().get(this.getRoute().size()-1).y == 8)
            this.getRoute().get(this.getRoute().size()-1).setDepot(true);

        int index = 0;
        LocalDateTime start = this.getStartDate(), curr;
        Depot depot;
        for (int i = 0; i < this.depots.size(); i++) {
            depot = this.depots.get(i);
            curr = depot.getRefillDate();
            long seconds = Duration.between(start, curr).toSeconds();

            int cantOrders = 0;
            for (Order o : this.orders) {
                if (o.deliveryDate.isAfter(depot.getRefillDate())) break;
                cantOrders++;
            }
            index = (int) ((seconds - cantOrders * 10 * 60.0) / 72);
            int x = this.getRoute().get(index).x, y = this.getRoute().get(index).y;
            if (!((x == 12 && y == 8) || (x == 42 && y == 42) || (x == 63 && y == 3))) {
                System.out.println("Planta mal identificada");
                System.out.println("start="+start+", curr="+curr+", index="+index);
                System.out.println("i="+i+", seconds="+seconds+", cantOrders="+cantOrders);
                continue;
            }
            depot.setIndexRoute(index);
            if (index < this.getRoute().size())
                this.getRoute().get(index).setDepot(true);
            else System.out.println("Indice menor a tamaño: "+index);
            /*if (index >= this.route.size()-4) index = this.route.size()-1;
            if (i != this.depots.size()-1) {
                Order nextOrder = this.orders.get(i+1);
                if (nextOrder.getX() == order.getX() &&
                        nextOrder.getY() == order.getY() &&
                        nextOrder.getDeliveryDate().isAfter(order.getDeliveryDate().plusMinutes(7)) &&
                        nextOrder.getDeliveryDate().isBefore(order.getDeliveryDate().plusMinutes(13))) {
                    nextOrder.setIndexRoute(index);
                    if (index < this.getRoute().size())
                        this.getRoute().get(index).setOrder(true);
                    start = start.plusMinutes(10);
                    i++;
//                    continue;
                }
            }*/
        }

        //añadir glp a nodos si es pedido o planta intermedia
        int cantDepots = 0, cantOrders = 0;
        for(Node node : route){
            //planta intermedia
            if(node.isDepot() && node.x != 12){
                if(depots.size()<=cantDepots){
                    System.out.println("Size de Depots: "+depots.size()+", Cant de depots: "+cantDepots);
                    continue;
                }
                node.setGlp(depots.get(cantDepots).refilledGlp);
                cantDepots++;
                continue;
            }
            //order
            if(node.isOrder()){
                if(orders.size()<=cantOrders){
                    System.out.println("Size de Orders: "+orders.size()+", Cant de orders: "+cantOrders);
                    continue;
                }
                node.setGlp(orders.get(cantOrders).getDelivered());
                cantOrders++;
            }
        }

        //generar nodos esquina
        cornerNodes = new ArrayList<>();
        Node nodeBefore = null;
        boolean horiz = false;
        boolean orderOrDepotBefore = false;
        int cant = 0;
        for(Node node : route){
            if(node.isDepot() && cant == 0){
                cornerNodes.add(new Node(node.x, node.y, false, true, node.glp));
                orderOrDepotBefore = true;
                cant++;
                continue;
            }

            if(nodeBefore == null){
                if(node.x == 12) horiz = false;
                else if(node.y == 8) horiz = true;
                else System.out.println("fallo nodeBefore == null");
            }
            else{
                //rumbo nuevo: vert
                if(node.x == nodeBefore.x){
                    //cambio de rumbo: horiz -> vert
                    if(horiz){
                        if(orderOrDepotBefore) orderOrDepotBefore = false;
                        else cornerNodes.add(new Node(nodeBefore.x, nodeBefore.y, false, false, node.glp));
                        horiz = false;
                    }
                }
                //rumbo nuevo: horiz
                else if(node.y == nodeBefore.y){
                    //cambio de rumbo: vert -> horiz
                    if(!horiz){
                        if(orderOrDepotBefore) orderOrDepotBefore = false;
                        else cornerNodes.add(new Node(nodeBefore.x, nodeBefore.y, false, false, node.glp));
                        horiz = true;
                    }
                }
                else System.out.println("fallo nodeBefore != null");
            }
            nodeBefore = new Node(node.x, node.y, node.order, node.depot, node.glp);
            if(orderOrDepotBefore) orderOrDepotBefore = false;

            if(node.isDepot() && cant > 0){
                cornerNodes.add(new Node(node.x, node.y, false, true, node.glp));
                orderOrDepotBefore = true;
            }
            else if(node.isOrder()){
                cornerNodes.add(new Node(node.x, node.y, true, false, node.glp));
                orderOrDepotBefore = true;
            }
            cant++;
        }
    }

    public void generateTruckGlpCapacity(List<TruckModelDto> truckModels){
        String truckModelActual = this.truckCode.substring(0,2);
        for(TruckModelDto tmd : truckModels){
            if(tmd.getCode().compareTo(truckModelActual) == 0){
                truckGlpCapacity = tmd.getCapacity();
                break;
            }
        }
    }
}
