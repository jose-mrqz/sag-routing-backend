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
    double demand;
    double totalDemand;
    LocalDateTime twOpen;
    LocalDateTime twClose;
    LocalDateTime deliveryTime;
    boolean visited = false;

    public Order(int x, int y, int idx, double demand,
                 LocalDateTime twOpen, LocalDateTime twClose) {
        super(x, y, idx);
        this.twOpen = twOpen;
        this.twClose = twClose;
        this.demand = demand;
        this.totalDemand = demand;
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
        visited = false;
        demand = totalDemand;
        deliveryTime = null;
    }
}
