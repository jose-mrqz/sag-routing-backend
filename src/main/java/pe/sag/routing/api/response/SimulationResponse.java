package pe.sag.routing.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.shared.dto.RouteDto;
import pe.sag.routing.shared.util.SimulationData;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationResponse {
    private SimulationData info;
    private List<RouteDto> routesReal;
    private List<RouteDto> routesTransformed;
}