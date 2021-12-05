package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.sag.routing.algorithm.Depot;
import pe.sag.routing.algorithm.Route;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SimulationHelper {
     private HashMap<String, Breakdown> breakdowns;
     private ArrayList<Depot> depots;
     private int count = 0;
     private int truckCount = 0;
     private boolean first = false;
     private boolean second = false;
     private boolean collapse = false;
     private LocalDateTime startDate;
     private LocalDateTime lastDate;
     private List<Route> routes;

     public SimulationHelper(boolean collapse) {
          breakdowns = new HashMap<>();
          depots = new ArrayList<>();
          this.collapse = collapse;
          this.routes = new ArrayList<>();
          count = 0;
     }

}

