package pe.sag.routing.algorithm;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderInfo extends NodeInfo {
    String id;
    LocalDateTime deliveryDate;
    double deliveredGlp;

    public OrderInfo(int x, int y, String id, LocalDateTime deliveryDate, double glp, LocalDateTime arrival) {
        super(x, y, arrival);
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.deliveredGlp = glp;
    }
}
