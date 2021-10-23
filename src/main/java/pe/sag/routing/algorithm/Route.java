package pe.sag.routing.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    String truckId;
    ArrayList<Node> nodes;
    ArrayList<NodeInfo> nodeInfos;
    ArrayList<Pair<Integer,Integer>> path;
    ArrayList<LocalDateTime> times;
    int totalTourDistance;
    double totalFuelConsumption;
    double totalDelivered;
    LocalDateTime startDate;
    LocalDateTime finishDate;

    public Route (String truckId, LocalDateTime startDate, LocalDateTime finishDate,
                  ArrayList<NodeInfo> nodeInfos) {
        this.truckId = truckId;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.nodeInfos = nodeInfos;
    }

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
            while (xi != xf) {
                if (xi > xf) xi--;
                else xi++;
                path.add(new Pair<>(xi, yi)) ;
            }
            while (yi != yf) {
                if (yi > yf) yi--;
                else yi++;
                path.add(new Pair<>(xi, yi)) ;
            }
        }
    }
}
