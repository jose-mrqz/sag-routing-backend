package pe.sag.routing.core.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FuelConsume {
    private LocalDateTime date;
    private double fuelConsumed;
}

