package pe.sag.routing.algorithm;


import java.time.LocalDateTime;

public class DepotInfo extends NodeInfo {
    Double refilledGlp;

    public DepotInfo(int x, int y, double fuel, double glp, LocalDateTime arrival) {
        super(x, y, fuel, arrival);
        refilledGlp = glp;
    }
}
