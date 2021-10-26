package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int speed;
}
