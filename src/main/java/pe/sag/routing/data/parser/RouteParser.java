package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Route;
import pe.sag.routing.shared.dto.RouteDto;

public class RouteParser {
    public static Route fromDto(RouteDto routeDto) {
        return BaseParser.parse(routeDto, Route.class);
    }

    public static RouteDto toDto(Route route) {
        return BaseParser.parse(route, RouteDto.class);
    }
}
