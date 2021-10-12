package pe.sag.routing.algorithm;

import java.time.LocalDateTime;
import java.util.List;

public class Planner {
    public static void main(String[] args) {
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

        Truck t1 = new Truck(5.0, 1.0, 0, LocalDateTime.now());
        Truck t2 = new Truck(5.0, 1.0, 0, LocalDateTime.now());
        List<Truck> trucks = List.of(t1, t2);

        Colony colony = new Colony(orders, trucks, LocalDateTime.now());
        colony.run();

        List<Route> solutionTrucks = colony.solutionTrucks;
        List<Node> solutionNodes = colony.solutionNodes;
        System.out.println("Listo");
    }
}
