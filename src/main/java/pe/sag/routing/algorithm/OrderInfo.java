package pe.sag.routing.algorithm;

import java.time.LocalDateTime;

public class OrderInfo extends NodeInfo {
    String id;
    LocalDateTime deliveryDate;
    double deliveredGlp;

    public OrderInfo(int x, int y, double fuel, String id, LocalDateTime deliveryDate, double glp, LocalDateTime arrival) {
        super(x, y, fuel, arrival);
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.deliveredGlp = glp;
    }
}
