package pe.sag.routing.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationRequest {
    private LocalDateTime actualDate;
    private int speed;
}
