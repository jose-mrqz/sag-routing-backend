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
    String truckCode;
    ArrayList<NodeInfo> nodesInfo;
    ArrayList<Pair<Integer,Integer>> path;
    double totalFuelConsumption;
    double totalDelivered;
    LocalDateTime startDate;
    LocalDateTime finishDate;

    public Route (String truckId, String truckCode, LocalDateTime startDate, LocalDateTime finishDate,
                  ArrayList<NodeInfo> nodesInfo, double totalFuelConsumption, double totalDelivered) {
        this.truckId = truckId;
        this.truckCode = truckCode;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.nodesInfo = nodesInfo;
        this.totalFuelConsumption = totalFuelConsumption;
        this.totalDelivered = totalDelivered;
    }

    public void generatePath() {
        path = new ArrayList<>();
        Pair<Integer,Integer> startingPoint = new Pair<>(12, 8);
        Pair<Integer,Integer> endingPoint;
        path.add(startingPoint);
        for (int i = 0; i <= nodesInfo.size(); i++) {
            if (i != nodesInfo.size()) endingPoint = new Pair(nodesInfo.get(i).x, nodesInfo.get(i).y);
            else endingPoint = new Pair<>(12,8);
            int xi = startingPoint.x;
            int yi = startingPoint.y;
            int xf = endingPoint.x;
            int yf = endingPoint.y;
            while (xi != xf) {
                if (xi > xf) xi--;
                else xi++;
                path.add(new Pair<>(xi, yi));
            }
            while (yi != yf) {
                if (yi > yf) yi--;
                else yi++;
                path.add(new Pair<>(xi, yi));
            }
            startingPoint = endingPoint;
        }
    }
}
