package pe.sag.routing.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.shared.dto.RouteDto;

import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationResponse {
    private List<RouteDto> routes1;
    private ArrayList<RouteDto> routes2;
}
