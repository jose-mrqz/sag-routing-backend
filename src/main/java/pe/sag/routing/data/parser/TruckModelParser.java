package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.TruckModel;
import pe.sag.routing.shared.dto.TruckModelDto;

public class TruckModelParser {
    public static TruckModel fromDto(TruckModelDto truckModelDto) {
        return BaseParser.parse(truckModelDto, TruckModel.class);
    }

    public static TruckModelDto toDto(TruckModel truckModel) {
        return BaseParser.parse(truckModel, TruckModelDto.class);
    }
}
