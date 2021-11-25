package pe.sag.routing.algorithm;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderInfo extends NodeInfo {
    String id;
    LocalDateTime deliveryDate;
    LocalDateTime deadlineDate;
    double deliveredGlp;

    public OrderInfo(int x, int y, String id, LocalDateTime deliveryDate, LocalDateTime deadlineDate, double glp, LocalDateTime arrival) {
        super(x, y, arrival);
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.deadlineDate = deadlineDate;
        this.deliveredGlp = glp;
    }
}
