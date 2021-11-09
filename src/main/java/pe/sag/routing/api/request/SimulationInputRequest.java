package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.shared.dto.RoadblockDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationInputRequest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimulationOrder {
        int x;
        int y;
        LocalDateTime date;
        double demandGLP;
        int slack;
    }

    private List<SimulationOrder> orders;
    private List<RoadblockDto> roadblocks;
    private int speed;
    private boolean colapse;
}
