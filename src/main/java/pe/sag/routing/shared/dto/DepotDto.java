package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import pe.sag.routing.core.model.TruckModel;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepotDto {
    @NotBlank
    private int x;
    @NotBlank
    private int y;
    @NotBlank
    private String name;
    @NotBlank
    private double glpCapacity;
    @NotBlank
    private double currentGlp;
}