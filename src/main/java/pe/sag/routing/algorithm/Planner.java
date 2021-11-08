package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

        for (pe.sag.routing.core.model.Truck tm : modelTrucks) {
            trucks.add(new Truck(tm.get_id(), tm.getCode(), tm.getModel().getCapacity(),
                    tm.getModel().getTareWeight(), 0, tm.getLastRouteEndTime()));
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

            solutionOrders = colony.solutionOrders;

            orders = new ArrayList<>();
            for (Order order : solutionOrders) {
                solutionTimes.add(new Pair<>(order._id, order.deliveryTime));
                orders.add(new Order(order));
            }

            List<Route> solutionRoutes = colony.solutionRoutes;

            if (solutionRoutes != null && solutionRoutes.size() != 0) {
                AStar aStar = new AStar();
                // route validation
                this.solutionRoutes.addAll(aStar.run(solutionRoutes, orders, roadblocks));
                for (int i = 0; i < trucks.size(); i++) {
                    Truck truck = trucks.get(i);
                    for (Route route : solutionRoutes) {
                        if (route.truckId.equals(truck.get_id())) {
                            truck.nowTime = route.getFinishDate();
                            truck.startDate = route.getFinishDate();
                            truck.startingDate = route.getFinishDate();
                        }
                    }
                }
            } else {
                break;
            }
        }

        for (Order order : solutionOrders) {
            solutionTimes.add(new Pair<>(order._id, order.deliveryTime));
        }


//        if (solutionRoutes != null) {
//            for (Route route : solutionRoutes) {
//                route.generatePath();
//            }
//        }
    }

    private boolean allVisited(List<Order> orders) {
        for (Order order : orders) {
            if (!order.visited) return false;
        }
        return true;
    }
}
