package pe.sag.routing.algorithm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Depot extends Node {
    private static final double DEPOT_CAPACITY = 160.0;

    HashMap<LocalDate, Double> remainingGlp;
    boolean isMain;

    public Depot(boolean isMain) {
        remainingGlp = new HashMap<>();
        this.isMain = isMain;
    }

    public double getAvailableGLp(LocalDate time) {
        double currentCapacity = remainingGlp.getOrDefault(time, -1.0);
        if (currentCapacity == -1.0) {
            if (!isMain) {
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
        remainingGlp.clear();
    }
}
