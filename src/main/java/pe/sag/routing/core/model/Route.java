package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
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

    public Route (pe.sag.routing.algorithm.Route route) {
        this.truckId = route.getTruckId();
        this.startDate = route.getStartDate();
        this.finishDate = route.getFinishDate();
        this.deliveredGLP = route.getTotalDelivered();
        this.fuelConsumed = route.getTotalFuelConsumption();
        this. nodes = route.getPath();
        this.orders = new ArrayList<>();

        for (NodeInfo nf : route.getNodesInfo()) {
            if (nf instanceof OrderInfo) {
                OrderInfo orderInfo = (OrderInfo)nf;
                this.orders.add(new Order(orderInfo.getId(), orderInfo.getX(),
                        orderInfo.getY(), orderInfo.getArrivalTime(),
                        orderInfo.getDeliveredGlp()));
            }
        }
    }

    @Id
    private String _id;
    @Indexed(unique = true)
    private String truckId;
    private List<Order> orders;
    private List<Pair<Integer,Integer>> nodes;
    private double fuelConsumed;
    private double deliveredGLP;
    private boolean active = true;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private boolean monitoring = true;
}
