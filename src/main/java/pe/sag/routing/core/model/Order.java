package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;

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
    private LocalDateTime registrationDate;
    private LocalDateTime deadlineDate;
    private LocalDateTime deliveryDate;
    private OrderStatus status;
    private boolean active = true;
}
