package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String code;
    private double demand;
    private int x;
    private int y;
    private LocalDateTime registrationDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime deadlineDate;
    private OrderStatus status;
    private boolean active = true;
}
