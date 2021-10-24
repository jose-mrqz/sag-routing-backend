package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    List<Route> solutionRoutes;

    public Planner(List<pe.sag.routing.core.model.Truck> modelTrucks,
                   List<pe.sag.routing.core.model.Order> modelOrders) {
        this.modelTrucks = modelTrucks;
        this.modelOrders = modelOrders;
    }

    public void run() {
        ArrayList<Truck> trucks = new ArrayList<>();
        ArrayList<Order> orders = new ArrayList<>();

        modelTrucks.forEach(tm -> trucks.add(new Truck(tm.get_id(), tm.getModel().getCapacity(),
                tm.getModel().getTareWeight(), 0, LocalDateTime.now())));

        AtomicInteger idx = new AtomicInteger();
        modelOrders.forEach(om -> orders.add(new Order(om.get_id(), om.getX(), om.getY(), idx.getAndIncrement(),
                om.getDemandGLP(), om.getRegistrationDate(), om.getDeadlineDate())));

        Colony colony = new Colony(orders, trucks);
        colony.run();

        solutionRoutes = colony.solutionRoutes;
        for (int i = 0; i < solutionRoutes.size(); i++) {
            Route route = solutionRoutes.get(i);
            route.generatePath();
        }
    }
}
