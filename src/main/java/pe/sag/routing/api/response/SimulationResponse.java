package pe.sag.routing.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.shared.dto.RouteDto;
import pe.sag.routing.shared.dto.TruckDto;
import pe.sag.routing.shared.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SimulationResponse {
    private SimulationData info;
    private List<TruckRoutes> routesReal;
    private List<TruckRoutes> routesTransformed;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TruckRoutes {
        String truckCode;
        List<RouteDto> routes;
    }

    public SimulationResponse(SimulationData info, List<RouteDto> routesReal, ArrayList<RouteDto> routesTransformed) {
        this.info = info;
        this.routesReal = group(routesReal);
        this.routesTransformed = group(routesTransformed);
    }

    private List<TruckRoutes> group(List<RouteDto> routeList) {
        HashMap<String, List<RouteDto>> routes = new HashMap<String, List<RouteDto>>();
        List<RouteDto> ptr;
        for (RouteDto route : routeList) {
            ptr = routes.getOrDefault(route.getTruckCode(), null);
            if (ptr == null) {
                routes.put(route.getTruckCode(), new ArrayList());
            }
            ptr = routes.get(route.getTruckCode());
            ptr.add(route);
            routes.put(route.getTruckCode(), ptr);
        }
        for (String key : routes.keySet()) {
            ptr = routes.get(key);
            ptr = ptr.stream().sorted(Comparator.comparing(RouteDto::getEndDate)).collect(Collectors.toList());
            routes.put(key, ptr);
        }
        List<TruckRoutes> truckRoutes = new ArrayList<>();
        for (String key : routes.keySet()) {
            TruckRoutes tr = new TruckRoutes(key, routes.get(key));
            truckRoutes.add(tr);
        }
        return truckRoutes;
    }
}
