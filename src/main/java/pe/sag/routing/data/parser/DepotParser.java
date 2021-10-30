package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.Depot;
import pe.sag.routing.shared.dto.DepotDto;

import java.time.LocalDateTime;
import java.util.HashMap;

public class DepotParser {
    private static final double MAX_CAPACITY = 160.0;

    public static Depot fromDto(DepotDto depotDto) {
        Depot depot = new Depot();
        depot.setX(depotDto.getX());
        depot.setY(depotDto.getY());
        depot.setName(depotDto.getName());
        depot.setGlpCapacity(depotDto.getGlpCapacity());
        return depot;
    }

    public static DepotDto toDto(Depot depot) {
        DepotDto depotDto = new DepotDto();
        depotDto.setX(depot.getX());
        depotDto.setY(depot.getY());
        depotDto.setName(depot.getName());
        depotDto.setGlpCapacity(depot.getGlpCapacity());
        depotDto.setCurrentGlp(depot.getCurrentGlp().getOrDefault(LocalDateTime.now(), MAX_CAPACITY));
        return depotDto;
    }
}