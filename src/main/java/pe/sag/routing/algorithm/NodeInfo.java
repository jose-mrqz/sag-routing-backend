package pe.sag.routing.algorithm;

import java.time.LocalDateTime;

public abstract class NodeInfo {
    int x;
    int y;
    double fuelConsumed;
    LocalDateTime arrivalTime;

    public NodeInfo(int x, int y, double fuel, LocalDateTime arrivalTime) {
        this.x = x;
        this.y = y;
        this.fuelConsumed = fuel;
        this.arrivalTime = arrivalTime;
    }
}
