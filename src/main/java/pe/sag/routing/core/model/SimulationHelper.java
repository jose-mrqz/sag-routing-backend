package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pe.sag.routing.algorithm.Depot;
import pe.sag.routing.algorithm.OrderInfo;
import pe.sag.routing.algorithm.Pair;
import pe.sag.routing.algorithm.Route;

import java.time.Duration;
import java.time.LocalDate;
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
     private HashMap<LocalDate, List<Double>> ratios;
     private HashMap<LocalDate, Double> indicators;

     public SimulationHelper(boolean collapse) {
          breakdowns = new HashMap<>();
          depots = new ArrayList<>();
          ratios = new HashMap<>();
          indicators = new HashMap<>();
          this.collapse = collapse;
          this.routes = new ArrayList<>();
          count = 0;
     }

     public void addOrders(List<Order> pendingOrders, List<Pair<String, LocalDateTime>> solutionOrders) {
          if (ratios == null) ratios = new HashMap<>();
          for (Order o : pendingOrders) {
               for (Pair<String, LocalDateTime> delivery : solutionOrders) {
                    if (delivery.getX().compareTo(o.get_id()) == 0 && delivery.getY() != null) {
                         double eta = Duration.between(o.getRegistrationDate(), delivery.getY()).toSeconds();
                         double limit = Duration.between(o.getRegistrationDate(), o.getDeadlineDate()).toSeconds();

                         List<Double> aux = ratios.getOrDefault(o.getRegistrationDate().toLocalDate(), new ArrayList<>());
                         aux.add(eta/limit);
                         ratios.put(o.getRegistrationDate().toLocalDate(), aux);

                         break;
                    }
               }
          }
          updateIndicators();
     }

     private void updateIndicators() {
          ratios.forEach((k, v) -> {
               double value = 0.0;
               if (v.size() > 0) {
                   value = v.stream().reduce(0.0, Double::sum)/v.size();
               }
               indicators.put(k, value);
          });
     }
}

