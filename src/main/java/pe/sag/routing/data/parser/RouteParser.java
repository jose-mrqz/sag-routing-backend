package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Route;
import pe.sag.routing.shared.dto.RouteDto;

import java.time.format.DateTimeFormatter;

public class RouteParser {
    public static RouteDto toDto(Route route) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        RouteDto routeDto = new RouteDto();
        routeDto.setStartDate(route.getStartDate().format(format));
        routeDto.setEndDate(route.getFinishDate().format(format));
        routeDto.setTruckCode("ACM1PT4RD0");
        routeDto.setOrders(route.getOrders());
        routeDto.setNodes(route.getNodes());
        return routeDto;
    }
}
