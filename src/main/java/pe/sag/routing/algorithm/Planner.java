package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import pe.sag.routing.aStar.AStar;
import pe.sag.routing.api.controller.RouteController;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.Breakdown;
import pe.sag.routing.core.model.Roadblock;
import pe.sag.routing.core.model.SimulationHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private boolean isSimulation = false;

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders,
                   List<Roadblock> roadblocks,
                   List<pe.sag.routing.core.model.Depot> modelDepots) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
        this.roadblocks = roadblocks;
        this.modelDepots = modelDepots;
    }

    public void run() {
        List<Truck> trucks = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        List<Depot> solutionDepots = null;

        modelTrucks = modelTrucks.stream()
                .sorted(Comparator.comparing(pe.sag.routing.core.model.Truck::getModelCapacity)
                        .thenComparing(pe.sag.routing.core.model.Truck::getLastRouteEndTime))
                .collect(Collectors.toList());

//        modelOrders = modelOrders.stream()
//                .sorted(Comparator.comparing(pe.sag.routing.core.model.Order::getDemandGLP)
//                    .thenComparing(pe.sag.routing.core.model.Order::getRegistrationDate))
//                .collect(Collectors.toList());

//        HashMap<Integer, Integer> count = new HashMap<>();
//        for (pe.sag.routing.core.model.Order o : modelOrders) {
//            if (o.getTotalDemand() > 15.0) {
//                count.put(10, count.getOrDefault(25, 0)+1);
//            } else if (o.getTotalDemand() > 10.0) {
//                count.put(15, count.getOrDefault(15, 0)+1);
//            } else if (o.getTotalDemand() > 5.0) {
//                count.put(25, count.getOrDefault(10, 0)+1);
//            } else {
//                count.put(5, count.getOrDefault(5, 0)+1);
//            }
//        }

//        HashMap<String, List<pe.sag.routing.core.model.Truck>> truckCategory = new HashMap<>();
//        for (int i = 0; i < modelTrucks.size(); i++) {
//            pe.sag.routing.core.model.Truck t = modelTrucks.get(i);
//            List<pe.sag.routing.core.model.Truck> list = truckCategory.getOrDefault(t.getModel().get_id(), null);
//            if (list == null) {
//                list = new ArrayList<>();
//            }
//            list.add(t);
//            truckCategory.put(t.getModel().get_id(), list);
//        }

        Collections.reverse(modelTrucks);
//        Collections.reverse(modelOrders);
//            Collections.shuffle(modelTrucks);

        for (pe.sag.routing.core.model.Truck tm : modelTrucks) {
            trucks.add(new Truck(tm.get_id(), tm.getCode(), tm.getModel().getCapacity(),
                    tm.getModel().getTareWeight(), 0, tm.getLastRouteEndTime(), tm.getClosestMaintenanceStart()));
        }

//        modelOrders = modelOrders.stream().sorted(Comparator.comparing(pe.sag.routing.core.model.Order::getDeadlineDate).
//                thenComparing(pe.sag.routing.core.model.Order::getRegistrationDate)).collect(Collectors.toList());

        int k = 0;
        for (pe.sag.routing.core.model.Order om : modelOrders) {
            orders.add(new Order(om.get_id(), om.getX(), om.getY(), k++,
                    om.getDemandGLP(), om.getDeadlineDate(), om.getRegistrationDate(), om.getDeadlineDate()));
        }

        if (modelDepots != null) {
            solutionDepots = new ArrayList<>();
            solutionDepots.add(new Depot(modelDepots.get(0), 1));
            solutionDepots.add(new Depot(modelDepots.get(1), 2));
        }

        while (!allVisited(orders)) {
            if (isSimulation) {
                if (!RouteController.simulationHelper.isCollapse()) {
                    checkBreakdowns(trucks);
                }
            }


            Colony colony = new Colony(orders, trucks, solutionDepots, roadblocks);
            colony.run();

            if (colony.solutionRoutes == null || colony.solutionRoutes.size() == 0) break;

            List<Route> solutionRoutes = colony.getSolutionRoutes();
            AStar astar = new AStar();
            solutionRoutes = astar.run(solutionRoutes, solutionOrders, roadblocks);
            solutionDepots = colony.solutionDepots;
            orders = colony.solutionOrders;

            SimulationHelper sh = RouteController.simulationHelper;


            sh.setDepots(new ArrayList<>());
            for (Depot d : solutionDepots) {
                Depot clone = new Depot(d);
                sh.getDepots().add(clone);
            }

            if (isSimulation) {
                if (!RouteController.simulationHelper.isCollapse()) {
                    createBreakdowns(solutionRoutes, orders, solutionDepots);
                }
            }

            solutionOrders.addAll(colony.solutionOrders.stream().filter(o -> o.visited).collect(Collectors.toList()));

            orders = orders.stream().filter(o -> !o.visited).collect(Collectors.toList());
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                order.setIdx(i);
            }

            if (solutionRoutes != null && solutionRoutes.size() != 0) {
                for (int i = 0; i < solutionDepots.size(); i++) {
                    Depot depot = solutionDepots.get(i);
                    depot.originalState = new HashMap<>();
                    depot.getRemainingGlp().forEach((key, val) -> depot.originalState.put(key, val));
                }

                for (int i = 0; i < trucks.size(); i++) {
                    Truck truck = trucks.get(i);
                    truck.reset();
                    for (Route route : solutionRoutes) {
                        if (route.truckId.compareTo(truck.get_id()) == 0) {
                            truck.nowTime = route.getFinishDate();
                            truck.startDate = route.getFinishDate();
                            truck.startingDate = route.getFinishDate();
                            truck.finished = false;
                            break;
                        }
                    }
                }

                this.solutionRoutes.addAll(solutionRoutes);

                for (int i = 0; i < orders.size(); i++) {
                    Order order = orders.get(i);
                    if (order.visited) order.shouldReset = false;
                    else {
                        order.resetDemand = order.demand;
                    }
                }
            } else {
                break;
            }
        }

        HashMap<String, LocalDateTime> lazy = new HashMap<>();
        for (Order order : solutionOrders) {
            if (order.getDeliveryTime() != null) {
                if (lazy.getOrDefault(order._id, null) == null) {
                    nScheduled++;
                    lazy.put(order._id, order.deliveryTime);
                }
                solutionTimes.add(new Pair<>(order._id, order.deliveryTime));
            }
            else if (firstFailed == null) {
                firstFailed = new Order(order);
            }
        }
        if (orders.size() > 0 && firstFailed == null) {
            firstFailed = new Order(orders.get(0));
        }
    }

    private void checkBreakdowns(List<Truck> trucks) {
        SimulationHelper sh = RouteController.simulationHelper;
        for (int i = 0; i < trucks.size(); i++) {
            Truck t = trucks.get(i);
            for (String k : sh.getBreakdowns().keySet()) {
                if (t._id.compareTo(k) == 0) {
                    Breakdown breakdown = sh.getBreakdowns().get(k);
                    if (t.getStartingDate().isAfter(breakdown.getStartDate()) &&
                            t.getStartingDate().isBefore(breakdown.getEndDate().plusHours(48))) {
                        t.setStartDate(breakdown.getEndDate().plusHours(48));
                        t.setStartingDate(breakdown.getEndDate().plusHours(48));
                    }
                }
            }
        }
    }

    private void createBreakdowns(List<Route> routes, List<Order> orders, List<Depot> depots) {
        SimulationHelper sh = RouteController.simulationHelper;
        if (sh.getCount() >= 2) return;
        if (sh.getCount() == 0) { //second truck
            if (routes.size() < 2 && sh.getTruckCount() < 1) { //metida de ratinha
                sh.setCount(1);
                return;
            }
            Route route = routes.get(1);
            if (Duration.between(route.getStartDate(), route.getFinishDate()).toMinutes() <= 120) {
                sh.setCount(1);
                return;
            }
            sh.setCount(1);
            Breakdown breakdown = cancelRoute(route, 120, orders, depots);
            sh.getBreakdowns().put(route.getTruckId(), breakdown);
        }
        if (sh.getCount() == 1) { //fourth truck
            if (routes.size() < 4 || (sh.getTruckCount() + routes.size() < 4)) { //metida de ratinha
                sh.setCount(2);
                return;
            }
            Route route = routes.get(3);
            if (Duration.between(route.getStartDate(), route.getFinishDate()).toMinutes() <= 180) {
                sh.setCount(2);
                return;
            }
            sh.setCount(2);
            Breakdown breakdown = cancelRoute(route, 180, orders, depots);
            sh.getBreakdowns().put(route.getTruckId(), breakdown);
        }
        sh.setTruckCount(sh.getTruckCount() + routes.size());
    }

    private Breakdown cancelRoute(Route route, int minutes, List<Order> orders, List<Depot> depots) {
        LocalDateTime now = route.getStartDate().plusMinutes(minutes);
        NodeInfo nextNode = route.getNodesInfo().stream()
                .filter(n -> n.arrivalTime.isAfter(now))
                .findFirst()
                .orElse(null);
        int traveledNodes = (int) (Duration.between(route.getStartDate(), now).toSeconds() / 0b1001000); // wtf
        if (traveledNodes < 0) traveledNodes = 0;
        if (traveledNodes >= route.getPath().size()) traveledNodes = route.getPath().size()-1;
        Breakdown breakdown = Breakdown.builder()
                .x(route.getPath().get(traveledNodes).getX())
                .y(route.getPath().get(traveledNodes).getY())
                .routeId(null)
                .truckCode(route.getTruckCode())
                .startDate(now)
                .endDate(now.plusMinutes(60))
                .build();
        List<Pair<Integer,Integer>> realPath = route.getPath().subList(0, traveledNodes);
        route.setPath(new ArrayList<>(realPath));
        if (nextNode != null) {
            List<NodeInfo> pendingNodes = route.getNodesInfo().subList(route.getNodesInfo().indexOf(nextNode), route.getNodesInfo().size());
            for (NodeInfo ni : pendingNodes) {
                if (ni instanceof OrderInfo) {
                    revertOrder(orders, ((OrderInfo)ni).getId(), ((OrderInfo)ni).getDeliveredGlp());
                } else if (ni instanceof DepotInfo) {
                    revertDepot(depots, ((DepotInfo)ni).getId(), ((DepotInfo)ni).getRefilledGlp(), ni.getArrivalTime());
                }
            }
            route.nodesInfo.removeAll(pendingNodes);
        }
        route.setFinishDate(route.getStartDate().plusMinutes(minutes));
        return breakdown;
    }


    private void revertDepot(List<Depot> depots, String id, Double refilledGlp, LocalDateTime arrivalTime) {
        for (int i = 0; i < depots.size(); i++) {
            Depot depot = depots.get(i);
            if (depot.id.compareTo(id) == 0) {
                double capacity = depot.getAvailableGLp(arrivalTime.toLocalDate());
                depot.remainingGlp.put(arrivalTime.toLocalDate(), capacity + refilledGlp);
            }
        }
    }

    private void revertOrder(List<Order> orders, String id, double glp) {
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order._id.compareTo(id) == 0) {
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
