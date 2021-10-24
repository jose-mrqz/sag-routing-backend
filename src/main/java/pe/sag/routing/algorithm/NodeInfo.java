package pe.sag.routing.algorithm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public abstract class NodeInfo {
    int x;
    int y;
    LocalDateTime arrivalTime;

    public NodeInfo(int x, int y, LocalDateTime arrivalTime) {
        this.x = x;
        this.y = y;
        this.arrivalTime = arrivalTime;
    }
}
