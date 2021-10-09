package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckModelDto {
    @NotBlank
    private String code;
    @NotBlank
    private double capacityGLP;
    @NotBlank
    private double tareWeight;
    @NotBlank
    private double loadWeight;
    @NotBlank
    private double grossWeight;
    @NotBlank
    private double fuelCapacity;
}
