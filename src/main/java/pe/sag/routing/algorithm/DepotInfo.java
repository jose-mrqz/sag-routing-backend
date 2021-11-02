package pe.sag.routing.algorithm;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class DepotInfo extends NodeInfo {
    Double refilledGlp;
    String id;

    public DepotInfo(int x, int y, double glp, LocalDateTime arrival) {
        super(x, y, arrival);
        refilledGlp = glp;
    }
}
