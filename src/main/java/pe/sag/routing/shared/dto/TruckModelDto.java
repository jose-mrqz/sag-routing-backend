package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckModelDto {
    private String code;
    private double capacity;
    private double tareWeight;
    private double loadWeight;
    private double grossWeight;
    private double fuelCapacity;
}
