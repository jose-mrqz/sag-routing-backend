package pe.sag.routing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.model.Truck;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteDto {
    @NotBlank
    private Truck truck;
    @NotBlank
    private List<String> orders;
    @NotBlank
    private double distance;
    @NotBlank
    private double fuelConsumed;
    @NotBlank
    private double deliveredGLP;
    @NotBlank
    private List<Pair<Integer,Integer>> nodes;
}
