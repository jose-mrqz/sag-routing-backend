package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depot extends Node {
    private static final double DEPOT_CAPACITY = 160.0;

    HashMap<LocalDate, Double> remainingGlp = new HashMap<>();
    HashMap<LocalDate, Double> originalState = new HashMap<>();
    boolean isMain;
    String id;

    public Depot(boolean isMain, int x, int y, int idx) {
        originalState = new HashMap<>();
        remainingGlp = new HashMap<>();
        this.isMain = isMain;
        this.x = x;
        this.y = y;
        this.idx = idx;
        id = null;
    }

    public Depot(pe.sag.routing.core.model.Depot depot, int idx) {
        this.x = depot.getX();
        this.y = depot.getY();
        this.isMain = false;
        this.idx = idx;
        originalState.put(LocalDate.now(), depot.getCurrentGlp());
        remainingGlp.put(LocalDate.now(), depot.getCurrentGlp());
        id = depot.get_id();
    }

    public double getAvailableGLp(LocalDate time) {
        double currentCapacity = remainingGlp.getOrDefault(time, -1.0);
        if (currentCapacity == -1.0) {
            if (isMain) {
                remainingGlp.put(time, Double.MAX_VALUE);
                currentCapacity = Double.MAX_VALUE;
            } else {
                remainingGlp.put(time, DEPOT_CAPACITY);
                currentCapacity = DEPOT_CAPACITY;
            }
        }
        return currentCapacity;
    }

    public void handleVisit(LocalDateTime time, double missingGlp) {
        LocalDate now = time.toLocalDate();
        double availableGlp = getAvailableGLp(now);
        if (missingGlp >= availableGlp) {
            remainingGlp.put(now, 0.0);
        } else {
            remainingGlp.put(now, availableGlp - missingGlp);
        }
    }

    @Override
    public void reset() {
        remainingGlp = (HashMap<LocalDate, Double>) originalState.clone();
    }
}
