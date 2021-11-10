package pe.sag.routing.shared.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationData {
    private int nOrders;
    private int nScheduled;
    private String message;
    private boolean finished;
    private LocalDateTime lastRouteEndTime = null;
}
