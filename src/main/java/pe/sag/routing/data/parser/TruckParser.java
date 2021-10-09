package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Truck;
import pe.sag.routing.shared.dto.TruckDto;

public class TruckParser {
    public static Truck fromDto(TruckDto truckDto) {
        return BaseParser.parse(truckDto, Truck.class);
    }

    public static TruckDto toDto(Truck truck) {
        return BaseParser.parse(truck, TruckDto.class);
    }
}
