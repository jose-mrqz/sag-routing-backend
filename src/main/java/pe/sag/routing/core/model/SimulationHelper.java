package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.sag.routing.algorithm.Depot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@Builder
public class SimulationHelper {
     private HashMap<String, Breakdown> breakdowns;
     private ArrayList<Depot> depots;
     private int count = 0;
     private int truckCount = 0;
     private boolean collapse = false;
     private LocalDateTime startDate;

     public SimulationHelper(boolean collapse) {
          breakdowns = new HashMap<>();
          depots = new ArrayList<>();
          this.collapse = collapse;
          count = 0;
     }
}
