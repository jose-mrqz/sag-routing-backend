package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planner {
    private List<pe.sag.routing.core.model.Truck> modelTrucks;
    private List<pe.sag.routing.core.model.Order> modelOrders;
    private List<pe.sag.routing.core.model.Depot> modelDepots = null;
    private List<Roadblock> roadblocks = new ArrayList<>();
    List<Route> solutionRoutes = new ArrayList<>();
    List<Order> solutionOrders = new ArrayList<>();
    List<Pair<String, LocalDateTime>> solutionTimes = new ArrayList<>();
    private int nScheduled = 0;
    private int nOrders = 0;
    private Order firstFailed = null;

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders,
                   List<Roadblock> roadblocks,
                   List<pe.sag.routing.core.model.Depot> modelDepots) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
        this.roadblocks = roadblocks;
        this.modelDepots = modelDepots;
    }

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
    }

    public void run() {
        List<Truck> trucks = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        List<Depot> depots = null;
        nOrders = this.modelOrders.size();

        for (pe.sag.routing.core.model.Truck tm : modelTrucks) {
            trucks.add(new Truck(tm.get_id(), tm.getCode(), tm.getModel().getCapacity(),
                    tm.getModel().getTareWeight(), 0, tm.getLastRouteEndTime()));
            break;
        }
        modelOrders = modelOrders.stream().sorted(Comparator.comparing(pe.sag.routing.core.model.Order::getDeadlineDate).
                thenComparing(pe.sag.routing.core.model.Order::getRegistrationDate)).collect(Collectors.toList());

        int k = 0;
        for (pe.sag.routing.core.model.Order om : modelOrders) {
            orders.add(new Order(om.get_id(), om.getX(), om.getY(), k++,
                    om.getDemandGLP(), om.getRegistrationDate(), om.getDeadlineDate()));
        }

        if (modelDepots != null) {
            depots = new ArrayList<>();
            depots.add(new Depot(modelDepots.get(0), 1));
            depots.add(new Depot(modelDepots.get(1), 2));
        }

        while (!allVisited(orders)) {
            Colony colony = new Colony(orders, trucks, depots, roadblocks);
            colony.run();

            orders = colony.solutionOrders;
            depots = colony.solutionDepots;

            List<Route> solutionRoutes = colony.solutionRoutes;

            if (solutionRoutes != null && solutionRoutes.size() != 0) {
                AStar aStar = new AStar();
                // route validation
                List<Route> validatedRoutes = aStar.run(solutionRoutes, orders, roadblocks) ;

                for (int i = 0; i < validatedRoutes.size(); i++) {
                    Route route = validatedRoutes.get(i);
                    int pathLength = route.getPath().size();
                    Pair<Integer, Integer> lastNode = route.getPath().get(pathLength-1);
                    if (lastNode.x != 12 && lastNode.y != 8) { //redo route
                        // reset order and depot consumption
                        for (NodeInfo ni : route.getNodesInfo()) {
                            if (ni instanceof OrderInfo)
                                revertOrder(orders, ((OrderInfo) ni).getId(), ((OrderInfo) ni).getDeliveredGlp());
                            else
                                revertDepot(depots, ((DepotInfo) ni).getId(), ((DepotInfo) ni).getRefilledGlp(), ni.getArrivalTime());
                        }
                    }
                }
                for (int i = 0; i < depots.size(); i++) {
                    Depot depot = depots.get(i);
                    depot.originalState = (HashMap<LocalDate, Double>) depot.getRemainingGlp().clone();
                }

                for (int i = 0; i < trucks.size(); i++) {
                    Truck truck = trucks.get(i);
                    for (Route route : validatedRoutes) {
                        if (route.truckId.equals(truck.get_id())) {
                            truck.reset();
                            truck.nowTime = route.getFinishDate();
                            truck.startDate = route.getFinishDate();
                            truck.startingDate = route.getFinishDate();
                            truck.finished = false;
                        }
                    }
                }

                for (int i = 0; i < orders.size(); i++) {
                    Order order = orders.get(i);
                    if (order.visited) order.shouldReset = false;
                }

                this.solutionRoutes.addAll(validatedRoutes.stream()
                        .filter(r -> (r.getPath().get(r.getPath().size()-1).x == 12 && r.getPath().get(r.getPath().size()-1).y == 8))
                        .collect(Collectors.toList()));
                this.solutionOrders = orders;
            } else {
                break;
            }
        }

        for (Order order : solutionOrders) {
            solutionTimes.add(new Pair<>(order._id, order.deliveryTime));
            if (order.getDeliveryTime() != null) nScheduled++;
            else if (firstFailed == null) {
                firstFailed = new Order(order);
            }
        }
    }

    private void revertDepot(List<Depot> depots, String id, Double refilledGlp, LocalDateTime arrivalTime) {
        for (int i = 0; i < depots.size(); i++) {
            Depot depot = depots.get(i);
            if (depot.id.equals(id)) {
                double capacity = depot.getAvailableGLp(arrivalTime.toLocalDate());
                depot.remainingGlp.put(arrivalTime.toLocalDate(), capacity + refilledGlp);
            }
        }
    }

    private void revertOrder(List<Order> orders, String id, double glp) {
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order._id.equals(id)) {
                order.deliveryTime = null;
                order.visited = false;
                order.demand -= glp;
                order.resetDemand = order.demand;
            }
        }
    }

    private boolean allVisited(List<Order> orders) {
        for (Order order : orders) {
            if (!order.visited) {
                return false;
            }
        }
        return true;
    }
}
