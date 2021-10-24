package pe.sag.routing.algorithm;


import java.time.LocalDateTime;

public class DepotInfo extends NodeInfo {
    Double refilledGlp;

    public DepotInfo(int x, int y, double glp, LocalDateTime arrival) {
        super(x, y, arrival);
        refilledGlp = glp;
    }
}
