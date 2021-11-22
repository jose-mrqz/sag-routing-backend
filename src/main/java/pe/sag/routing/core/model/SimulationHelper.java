package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.sag.routing.algorithm.Depot;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@Builder
public class SimulationHelper {
     private HashMap<String, Breakdown> breakdowns;
     private ArrayList<Depot> depots;
     private int count = 0;

     public SimulationHelper() {
          breakdowns = new HashMap<>();
          depots = new ArrayList<>();
          count = 0;
     }
}
