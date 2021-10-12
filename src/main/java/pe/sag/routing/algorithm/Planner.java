package pe.sag.routing.algorithm;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import pe.sag.routing.core.service.TruckService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Planner {
    @Autowired
    private TruckService truckService;

    public Planner(){
    }

    public void run() {
        Order o1 = Order.builder()
                .demand(3.5)
                .totalDemand(3.5)
                .twOpen(LocalDateTime.now().plusMinutes(15))
                .twClose(LocalDateTime.now().plusMinutes(15).plusHours(4))
                .deliveryTime(null)
                .visited(false)
                .build();
        Order o2 = Order.builder()
                .demand(7.5)
                .totalDemand(7.5)
                .twOpen(LocalDateTime.now().plusMinutes(33))
                .twClose(LocalDateTime.now().plusMinutes(33).plusHours(5))
                .deliveryTime(null)
                .visited(false)
                .build();
        Order o3 = Order.builder()
                .demand(15)
                .totalDemand(15)
                .twOpen(LocalDateTime.now().plusMinutes(40))
                .twClose(LocalDateTime.now().plusMinutes(40).plusHours(4))
                .deliveryTime(null)
                .visited(false)
                .build();

        o1.x = 14;
        o3.x = 19;
        o2.x = 25;
        o1.y = 30;
        o3.y = 22;
        o2.y = 40;

        List<Order> orders = List.of(o1, o2, o3);

        //Read trucks
        List<pe.sag.routing.core.model.Truck> trucksModel = truckService.findByAvailable(true);
        ArrayList<Truck> trucks = new ArrayList<>();

        for(pe.sag.routing.core.model.Truck tm : trucksModel){
            Truck t = new Truck(tm.getModel().getCapacity(), tm.getModel().getTareWeight(), 0, LocalDateTime.now());
            trucks.add(t);
        }

        Colony colony = new Colony(orders, trucks, LocalDateTime.now());
        colony.run();

        List<Route> solutionTrucks = colony.solutionTrucks;
        List<Node> solutionNodes = colony.solutionNodes;
        System.out.println("Listo");
    }
}
