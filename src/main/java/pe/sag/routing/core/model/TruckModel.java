package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckModel {
    private static final double FUEL_CAPACITY = 25.0;

    @Id
    private String _id;
    @Indexed(unique = true)
    private String code;
    private double capacity;
    private double tareWeight;
    private double loadWeight;
    private double grossWeight;
    private double fuelCapacity = FUEL_CAPACITY;
    private boolean active = true;
}
