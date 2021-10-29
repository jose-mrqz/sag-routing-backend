package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.shared.dto.RoadblockDto;

public class RoadblockParser {
    public Roadblock fromDto(RoadblockDto roadblockDto) {
        return BaseParser.parse(roadblockDto, Roadblock.class);
    }

    public RoadblockDto toDto(Roadblock roadblock) {
        return BaseParser.parse(roadblock, RoadblockDto.class);
    }
}
