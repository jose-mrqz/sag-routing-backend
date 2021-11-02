package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.sag.routing.core.model.Roadblock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planner {
    private List<pe.sag.routing.core.model.Truck> modelTrucks;
    private List<pe.sag.routing.core.model.Order> modelOrders;
    private List<pe.sag.routing.core.model.Depot> modelDepots = null;
    private List<Roadblock> roadblocks = new ArrayList<>();
    List<Route> solutionRoutes;
    List<Pair<String,LocalDateTime>> solutionOrders;

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders,
                   List<Roadblock> roadblocks) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
        this.roadblocks = roadblocks;
    }

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
    }

    public void run() {
        ArrayList<Truck> trucks = new ArrayList<>();
        ArrayList<Order> orders = new ArrayList<>();
        ArrayList<Depot> depots = null;

        modelTrucks.forEach(tm -> trucks.add(new Truck(tm.get_id(), tm.getModel().getCapacity(),
                tm.getModel().getTareWeight(), 0, tm.getLastRouteEndTime())));

        AtomicInteger idx = new AtomicInteger();
        modelOrders.forEach(om -> orders.add(new Order(om.get_id(), om.getX(), om.getY(), idx.getAndIncrement(),
                om.getDemandGLP(), om.getRegistrationDate(), om.getDeadlineDate())));

        if (modelDepots != null) {
            depots = new ArrayList<>();
            depots.add(new Depot(modelDepots.get(0), 1));
            depots.add(new Depot(modelDepots.get(1), 2));
        }

        Colony colony = new Colony(orders, trucks, depots, roadblocks);
        colony.run();

        solutionRoutes = colony.solutionRoutes;
        solutionOrders = colony.solutionOrders;

        if (solutionRoutes != null) {
            for (Route route : solutionRoutes) {
                route.generatePath();
            }
        }
    }
}
