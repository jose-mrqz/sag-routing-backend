package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order extends Node {
    String _id;
    double demand;
    double totalDemand;
    LocalDateTime twOpen;
    LocalDateTime twClose;
    LocalDateTime deliveryTime;
    LocalDateTime deadlineTime;
    boolean visited = false;
    boolean shouldReset = true;
    int unloadTime = 10;

    double resetDemand = 0.0;

    public Order(String _id, int x, int y, int idx, double demand, LocalDateTime deadlineTime,
                 LocalDateTime twOpen, LocalDateTime twClose) {
        super(x, y, idx);
        this._id = _id;
        this.twOpen = twOpen;
        this.twClose = twClose;
        this.deadlineTime = deadlineTime;
        this.demand = demand;
        this.totalDemand = demand;
        this.resetDemand = this.totalDemand;
    }

    public Order(Order order) {
        super(order.x, order.y, order.idx);
        this._id = order._id;
        this.twOpen = order.twOpen;
        this.twClose = order.twClose;
        this.deliveryTime = order.deliveryTime;
        this.deadlineTime = order.deadlineTime;
        this.demand = order.demand;
        this.totalDemand = order.totalDemand;
        this.visited = order.visited;
        this.resetDemand = this.totalDemand;
    }

    public void handleVisit(LocalDateTime now, double glp) {
        if (glp >= demand) {
            demand = 0.0;
            visited = true;
            deliveryTime = now;
        } else {
            demand -= glp;
        }
    }

    public void reset() {
        if (shouldReset) {
            visited = false;
            demand = resetDemand;
            deliveryTime = null;
        }
    }
}
