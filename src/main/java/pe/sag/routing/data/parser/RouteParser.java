package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Route;
import pe.sag.routing.shared.dto.RouteDto;

public class RouteParser {
    public static RouteDto toDto(Route route) {
        RouteDto routeDto = new RouteDto();
        routeDto.setStartDate(route.getStartDate());
        routeDto.setEndDate(route.getFinishDate());
        routeDto.setTruckCode(route.getTruckCode());
        routeDto.setOrders(route.getOrders());
        routeDto.setNodes(route.getNodes());
        return routeDto;
    }
}
