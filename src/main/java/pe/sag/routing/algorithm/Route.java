package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    String truckId;
    ArrayList<Node> nodes;
    ArrayList<Pair<Integer,Integer>> path;
    ArrayList<LocalDateTime> times;
    int totalTourDistance;
    double totalFuelConsumption;
    double totalDelivered;
    LocalDateTime startDate;
    LocalDateTime finishDate;

    public void generatePath() {
        path = new ArrayList<>();
        Pair<Integer,Integer> startingPoint;
        Pair<Integer,Integer> endingPoint;
        for (int i = 1; i < nodes.size(); i++) {
            startingPoint = new Pair(nodes.get(i-1).x, nodes.get(i-1).y);
            endingPoint = new Pair(nodes.get(i).x, nodes.get(i).y);
            int xi = startingPoint.x;
            int yi = startingPoint.y;
            int xf = endingPoint.x;
            int yf = endingPoint.y;
            path.add(new Pair<>(xi, yi)) ;
            while (xi != xf || yi != yf) {
                int random = ThreadLocalRandom.current().nextInt(0, 2);
                if (random == 0) {
                    if (xi != xf) {
                        if (xi > xf) xi--;
                        else xi++;
                        path.add(new Pair<>(xi, yi)) ;
                    }
                } else {
                    if (yi != yf) {
                        if (yi > yf) yi--;
                        else yi++;
                        path.add(new Pair<>(xi, yi)) ;
                    }
                }
            }
        }
    }
}
