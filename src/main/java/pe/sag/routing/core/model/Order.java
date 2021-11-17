package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order extends Node {
    @Id
    private String _id;
    @Indexed(unique = true)
    private int code;
    private double demandGLP;
    private double totalDemand;
    private LocalDateTime registrationDate;
    private LocalDateTime deadlineDate;
    private LocalDateTime deliveryDate;
    private OrderStatus status;
    private boolean monitoring;
    private boolean active = true;

    public boolean inRoadblocks(List<Roadblock> roadblocks) {
        for (Roadblock r : roadblocks) {
            if (r.getX() == x && r.getY() == y) {
                if (!(deadlineDate.isBefore(r.getStartDate()) || registrationDate.isAfter(r.getEndDate())))
                    return true;
            }
        }
        return false;
    }
}
