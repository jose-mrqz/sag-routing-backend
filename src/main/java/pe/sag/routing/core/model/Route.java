package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.algorithm.DepotInfo;
import pe.sag.routing.algorithm.NodeInfo;
import pe.sag.routing.algorithm.OrderInfo;
import pe.sag.routing.algorithm.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order {
        String _id;
        int x;
        int y;
        LocalDateTime deliveryDate;
        double deliveredGlp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Depot {
        String _id;
        int x;
        int y;
        double refilledGlp;
    }

    public Route (pe.sag.routing.algorithm.Route route) {
        this.truckId = route.getTruckId();
        this.truckCode = route.getTruckCode();
        this.startDate = route.getStartDate();
        this.finishDate = route.getFinishDate();
        this.deliveredGlp = route.getTotalDelivered();
        this.fuelConsumed = route.getTotalFuelConsumption();
        this. nodes = route.getPath();
        this.orders = new ArrayList<>();
        this.depots = new ArrayList<>();

        for (NodeInfo ni : route.getNodesInfo()) {
            if (ni instanceof OrderInfo) {
                OrderInfo orderInfo = (OrderInfo)ni;
                orders.add(new Order(orderInfo.getId(), orderInfo.getX(),
                        orderInfo.getY(), orderInfo.getArrivalTime(),
                        orderInfo.getDeliveredGlp()));
            } else {
                DepotInfo depotInfo = (DepotInfo)ni;
                depots.add(new Depot(depotInfo.getId(), depotInfo.getX(), depotInfo.getY(), depotInfo.getRefilledGlp()));
            }
        }
    }

    @Id
    private String _id;
    @Indexed(unique = true)
    private String truckId;
    private String truckCode;
    private List<Order> orders;
    private List<Depot> depots;
    private List<Pair<Integer,Integer>> nodes;
    private double fuelConsumed;
    private double deliveredGlp;
    private double refilledGlp;
    private boolean cancelled = false;
    private boolean active = true;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private boolean monitoring = true;
}
