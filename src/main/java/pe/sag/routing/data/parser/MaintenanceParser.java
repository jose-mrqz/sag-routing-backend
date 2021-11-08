package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Maintenance;
import pe.sag.routing.shared.dto.MaintenanceDto;

public class MaintenanceParser {
    public static Maintenance fromDto(MaintenanceDto maintenanceDto) {
        return BaseParser.parse(maintenanceDto, Maintenance.class);
    }

    public static MaintenanceDto toDto(Maintenance maintenance) {
        return BaseParser.parse(maintenance, MaintenanceDto.class);
    }
}