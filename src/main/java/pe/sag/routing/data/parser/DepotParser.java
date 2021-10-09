package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Depot;
import pe.sag.routing.shared.dto.DepotDto;

public class DepotParser {
    public static Depot fromDto(DepotDto depotDto) {
        return BaseParser.parse(depotDto, Depot.class);
    }

    public static DepotDto toDto(Depot depot) {
        return BaseParser.parse(depot, DepotDto.class);
    }
}